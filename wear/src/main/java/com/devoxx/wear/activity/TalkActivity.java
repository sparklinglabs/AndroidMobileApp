package com.devoxx.wear.activity;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;

import com.devoxx.R;
import com.devoxx.common.utils.Constants;
import com.devoxx.event.AddFavoriteEvent;
import com.devoxx.event.TwitterEvent;
import com.devoxx.event.FavoriteEvent;
import com.devoxx.event.GetSpeakerEvent;
import com.devoxx.event.GetTalkEvent;
import com.devoxx.event.GetTalkSummaryEvent;
import com.devoxx.event.RemoveFavoriteEvent;
import com.devoxx.event.SpeakerDetailEvent;
import com.devoxx.event.TalkEvent;
import com.devoxx.event.TalkSummaryEvent;
import com.devoxx.model.TalkFullApiModel;
import com.devoxx.model.TalkSpeakerApiModel;
import com.devoxx.wear.adapter.TalkGridPageAdapter;
import com.devoxx.wear.wrapper.SpeakerDetailWrapper;
import com.devoxx.wear.wrapper.TalkWrapper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.LinkedHashMap;
import java.util.Map;

import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 06/09/15.
 */
public class TalkActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private final static String TAG = TalkActivity.class.getCanonicalName();


    // Google Play Services
    private GoogleApiClient mApiClient;

    // Layout widgets and adapters
    private TalkGridPageAdapter mTalkGridPageAdapter;
    private GridViewPager mPager;
    private DotsPageIndicator mDotsPageIndicator;

    // local cache
    private TalkFullApiModel mTalk;
    private LinkedHashMap<String, TalkSpeakerApiModel> mSpeakers = new LinkedHashMap<>();

    // data retrieved from the slot.
    // This will be used to initialize the layout and to add additional information to the talk
    private String mTalkId;
    private String mTalkTitle;
    private String mRoomName;
    private Long mFromTimeMillis;
    private Long mToTimeMillis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTalkId = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mTalkId = bundle.getString(Constants.DATAMAP_TALK_ID);
            mTalkTitle = bundle.getString(Constants.DATAMAP_TITLE);
            mRoomName = bundle.getString(Constants.DATAMAP_ROOM_NAME);
            mFromTimeMillis = bundle.getLong(Constants.DATAMAP_FROM_TIME_MILLIS);
            mToTimeMillis = bundle.getLong(Constants.DATAMAP_TO_TIME_MILLIS);
        }

        setContentView(R.layout.talk_activity);

        mPager = (GridViewPager) findViewById(R.id.pager);

        // we prepare the view with initial values gathered from the Slot
        mTalkGridPageAdapter = new TalkGridPageAdapter(this, getFragmentManager(), mTalkTitle);
        mPager.setAdapter(mTalkGridPageAdapter);

        mDotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        mDotsPageIndicator.setPager(mPager);
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (mTalk != null) {
            //The activity can have been awakened up by a notification (remove favorite).
            //In this case, we ensure that the favorites status did not changed.
            getFavoriteFromCache(mTalk.getId());
            return;
        }

        if (mTalkId == null) {
            return;
        }

        // Retrieve the talk
        getTalkFromCache(Constants.CHANNEL_ID + Constants.TALK_PATH + "/" + mTalkId);
    }


    @Override
    protected void onStart() {
        super.onStart();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        if (null != mApiClient && mApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mApiClient, this);
            mApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    protected void sendMessage(final String path, final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // broadcast the message to all connected devices
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message.getBytes()).await();

                }
            }
        }).start();
    }

    protected void sendMessage(final String path, final byte[] message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // broadcast the message to all connected devices
                final NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mApiClient, node.getId(), path, message);

                }
            }
        }).start();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

        for (DataEvent event : dataEventBuffer) {
            // Check if we have received our speakers
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.CHANNEL_ID + Constants.TALK_PATH + "/" + mTalkId)) {

                TalkWrapper talkWrapper = new TalkWrapper();

                final TalkFullApiModel talk = talkWrapper.getTalk(event);

                if (talk == null) {
                    return;
                }

                mTalk = talk;

                // add additional information coming from the Slot
                mTalk.setRoomName(mRoomName);
                mTalk.setFromTimeMillis(mFromTimeMillis);
                mTalk.setToTimeMillis(mToTimeMillis);

                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                // retrieve detail of each speaker
                for (TalkSpeakerApiModel speaker : mTalk.getSpeakers()) {
                    mSpeakers.put(speaker.getUuid(), speaker);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTalkGridPageAdapter.addSpeakers(mSpeakers);
                        mTalkGridPageAdapter.notifyDataSetChanged();
                    }
                });

                return;
            }


            // Check if we have received some details for a speaker
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.CHANNEL_ID + Constants.SPEAKER_PATH)) {

                if (mTalk == null) {
                    // ignore this message as we didn't processed the talk
                    return;
                }

                SpeakerDetailWrapper speakerDetailWrapper = new SpeakerDetailWrapper();

                TalkSpeakerApiModel speaker = speakerDetailWrapper.getSpeakerDetail(event);
                if (speaker == null) {
                    return;
                }

                if (mTalk.getSpeakers() == null) {
                    return;
                }

                // check if the speaker event is related to the current talk
                String speakerUuid = null;
                for (TalkSpeakerApiModel speakerTalk : mTalk.getSpeakers()) {
                    if (speakerTalk.getUuid().equalsIgnoreCase(speaker.getUuid())) {
                        speakerUuid = speaker.getUuid();
                        break;
                    }
                }

                if (speakerUuid == null) {
                    // this speaker event is not related to the talk
                    return;
                }

                mSpeakers.put(speaker.getUuid(), speaker);

                EventBus.getDefault().postLocal(new SpeakerDetailEvent(speaker));

                return;
            }

            // Event received when a change occurred in the favorite
            if (event.getType() == DataEvent.TYPE_CHANGED && event.getDataItem().getUri().getPath().startsWith(Constants.CHANNEL_ID + Constants.FAVORITE_PATH  + "/" + mTalkId)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                if (dataMapItem == null) {
                    return;
                }

                DataMap favoriteMap = dataMapItem.getDataMap().getDataMap(Constants.DETAIL_PATH);
                if (favoriteMap == null) {
                    return;
                }

                boolean favorite = favoriteMap.getBoolean(Constants.DATAMAP_FAVORITE);
                mTalk.setFavorite(favorite);
                EventBus.getDefault().postLocal(new FavoriteEvent(favorite));

                return;
            }
        }

    }


    // Get Talk from the data item repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getTalkFromCache(String pathToContent) {

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(pathToContent)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the talk from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                if (dataMap == null) {
                                    // unable to fetch data -> retrieve the talk from the Mobile
                                    sendMessage(Constants.CHANNEL_ID + Constants.TALK_PATH, mTalkId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the talk from the cache
                                TalkWrapper talkWrapper = new TalkWrapper();

                                final TalkFullApiModel talk = talkWrapper.getTalk(dataMap);

                                mTalk = talk;

                                // add additional information coming from the Slot
                                mTalk.setRoomName(mRoomName);
                                mTalk.setFromTimeMillis(mFromTimeMillis);
                                mTalk.setToTimeMillis(mToTimeMillis);

                                EventBus.getDefault().postLocal(new TalkEvent(mTalk));


                                if (mTalk.getSpeakers() != null) {
                                    // retrieve detail of each speaker
                                    for (TalkSpeakerApiModel speaker : mTalk.getSpeakers()) {
                                        mSpeakers.put(speaker.getUuid(), speaker);
                                    }
                                }

                                dataItems.release();

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTalkGridPageAdapter.addSpeakers(mSpeakers);
                                        mTalkGridPageAdapter.notifyDataSetChanged();

                                        // retrieve the status of the favorite
                                        getFavoriteFromCache(mTalk.getId());
                                    }
                                });
                            }
                        }
                );
    }


    // Get favorite status of the talk from the data item repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getFavoriteFromCache(final String  talkId) {

        final String dataPath = Constants.CHANNEL_ID + Constants.FAVORITE_PATH + "/" + talkId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the favorite from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                // retrieve the favorite from the cache
                                if (dataMap == null) {
                                    // Prepare the data map
                                    DataMap favoriteDataMap = new DataMap();
                                    favoriteDataMap.putString(Constants.DATAMAP_TALK_ID, talkId);

                                    // unable to fetch data -> retrieve the favorite status from the Mobile
                                    sendMessage(Constants.CHANNEL_ID + Constants.FAVORITE_PATH , talkId);
                                    dataItems.release();
                                    return;
                                }

                                DataMap favoriteMap = dataMap.getDataMap(Constants.DETAIL_PATH);
                                if (favoriteMap == null) {
                                    dataItems.release();
                                    return;
                                }

                                EventBus.getDefault().postLocal(new FavoriteEvent(favoriteMap.getBoolean(Constants.DATAMAP_FAVORITE, false)));

                                dataItems.release();
                            }
                        }
                );
    }


    // Get Speaker from the data items repository (cache).
    // If not available, we refresh the data from the Mobile device.
    //
    private void getSpeakerFromCache(final String speakerId) {

        final String dataPath = Constants.CHANNEL_ID + Constants.SPEAKER_PATH + "/" + speakerId;

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(dataPath)
                .build();

        Wearable.DataApi.getDataItems(mApiClient, uri)
                .setResultCallback(
                        new ResultCallback<DataItemBuffer>() {
                            @Override
                            public void onResult(DataItemBuffer dataItems) {

                                DataMap dataMap = null;
                                if (dataItems.getCount() > 0) {
                                    // retrieve the speaker from the cache
                                    dataMap = DataMap.fromByteArray(dataItems.get(0).getData());
                                }

                                if (dataMap == null) {
                                    // unable to fetch data -> refresh the list of slots from Mobile
                                    sendMessage(Constants.CHANNEL_ID + Constants.SPEAKER_PATH, speakerId);
                                    dataItems.release();
                                    return;
                                }

                                // retrieve and display the speaker from the cache
                                SpeakerDetailWrapper speakerDetailWrapper = new SpeakerDetailWrapper();

                                final TalkSpeakerApiModel speaker = speakerDetailWrapper.getSpeakerDetail(dataMap);

                                mSpeakers.put(speaker.getUuid(), speaker);

                                EventBus.getDefault().postLocal(new SpeakerDetailEvent(speaker));

                                dataItems.release();
                            }
                        }
                );
    }


    public Map<String, TalkSpeakerApiModel> getSpeakers() {
        return mSpeakers;
    }


    public TalkFullApiModel getTalk() {
        return mTalk;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    //
    // Events
    //

    public void onEvent(TwitterEvent twitterEvent) {

        // Retrieve the list of speakers from the mobile
        sendMessage(twitterEvent.getPath(), twitterEvent.getMessage());
    }


    public void onEvent(GetTalkEvent getTalkEvent) {
        EventBus.getDefault().postLocal(new TalkEvent(mTalk));
    }


    public void onEvent(GetTalkSummaryEvent getTalkSummaryEvent) {

        if (mTalk == null) {
            return;
        }

        EventBus.getDefault().postLocal(new TalkSummaryEvent(mTalk.getTitle(), mTalk.getSummary(), mTalk.getTalkType()));
    }

    public void onEvent(GetSpeakerEvent getSpeakerEvent) {

        if (getSpeakerEvent == null) {
            return;
        }

        getSpeakerFromCache(getSpeakerEvent.getUuid());
    }

    public void onEvent(AddFavoriteEvent addFavoritesEvent) {

        if (addFavoritesEvent == null) {
            return;
        }

        if (addFavoritesEvent.getTalkId() == null) {
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putString(Constants.DATAMAP_TALK_ID, addFavoritesEvent.getTalkId());

        sendMessage(Constants.CHANNEL_ID + Constants.ADD_FAVORITE_PATH, addFavoritesEvent.getTalkId());

    }

    public void onEvent(RemoveFavoriteEvent removeFavoritesEvent) {

        if (removeFavoritesEvent == null) {
            return;
        }

        if (removeFavoritesEvent.getTalkId() == null) {
            return;
        }

        DataMap dataMap = new DataMap();
        dataMap.putString(Constants.DATAMAP_TALK_ID, removeFavoritesEvent.getTalkId());

        sendMessage(Constants.CHANNEL_ID + Constants.REMOVE_FAVORITE_PATH, removeFavoritesEvent.getTalkId());

    }
}
