package com.devoxx.android.service;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.annimon.stream.Optional;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.devoxx.common.utils.Constants;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.connection.model.TalkSpeakerApiModel;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.conference.model.ConferenceDay;
import com.devoxx.data.manager.AbstractDataManager;
import com.devoxx.data.manager.NotificationsManager;
import com.devoxx.data.manager.SlotsDataManager;
import com.devoxx.data.manager.SpeakersDataManager;
import com.devoxx.data.model.RealmSpeaker;
import com.devoxx.event.ScheduleEvent;
import com.devoxx.utils.Logger;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import pl.tajchert.buswear.EventBus;

@EService
public class WearService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    private final static String TAG = WearService.class.getCanonicalName();

    @Bean
    ConferenceManager conferenceManager;

    @Bean
    SlotsDataManager slotsDataManager;

    @Bean
    SpeakersDataManager speakersDataManager;

    @Bean
    NotificationsManager notificationsManager;


    protected GoogleApiClient mApiClient;


    @Override
    public void onCreate() {
        super.onCreate();

        // Connect to Play Services
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onDestroy() {
        if ((mApiClient != null) && (mApiClient.isConnected())) {
            mApiClient.disconnect();
        }

        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Processing the incoming message
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());


        // send schedules to the Wearable
        if (path.startsWith(Constants.CHANNEL_ID + Constants.SCHEDULES_PATH)) {
            sendSchedules();
            return;
        }

        // send slots to the Wearable
        if (path.startsWith(Constants.CHANNEL_ID + Constants.SLOTS_PATH)) {

            try {
                sendSlots(Long.parseLong(data));

            } catch (Exception ex) {
                Log.e(TAG, ex.getLocalizedMessage());
            }

            return;
        }

        // send the talk to the Wearable
        if (path.startsWith(Constants.CHANNEL_ID + Constants.TALK_PATH)) {
            sendTalk(data);
            return;
        }


        // send the speaker to the Wearable
        if (path.startsWith(Constants.CHANNEL_ID + Constants.SPEAKER_PATH)) {
            sendSpeaker(data);
            return;
        }


        // send the favorite's status of a talk
        if (path.startsWith(Constants.CHANNEL_ID + Constants.FAVORITE_PATH)) {
            sendFavorite(data);
            return;
        }

        // Add the favorite's status to a talk
        if (path.equalsIgnoreCase(Constants.CHANNEL_ID + Constants.ADD_FAVORITE_PATH)) {
            addFavorite(data);
            return;
        }

        // Remove the favorite's status of a talk
        if (path.equalsIgnoreCase(Constants.CHANNEL_ID + Constants.REMOVE_FAVORITE_PATH)) {
            removeFavorite(data);
            return;
        }

        // Twitter
        if (path.equalsIgnoreCase(Constants.CHANNEL_ID + Constants.TWITTER_PATH)) {
            followOnTwitter(data);
            return;
        }

    }


    private void sendToWearable(final PutDataMapRequest putDataMapRequest) {

        if ((mApiClient != null) && (mApiClient.isConnected())) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
            return;
        }


        mApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {

                    }
                }).build();
        mApiClient.connect();

    }


    //
    // Twitter
    //

    // Open the Twitter application or the browser if the app is not installed
    private void followOnTwitter(String inputData) {
        String twitterName = inputData == null ? "" : inputData.trim().toLowerCase();
        twitterName = twitterName.replaceFirst("@", "");

        if (twitterName.isEmpty()) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitterName));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }



    private void sendSchedules() {

        // TODO: ensure that we have data

        final List<ConferenceDay> days = conferenceManager.getConferenceDays();

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.SCHEDULES_PATH);


        // set the header (timestamp is used to force a onDataChanged event on the wearable)
        final DataMap headerMap = new DataMap();
        headerMap.putString("timestamp", new Date().toString());
        putDataMapRequest.getDataMap().putDataMap(Constants.HEADER_PATH, headerMap);

        // Prepare and save the country code
        final DataMap countryMap = new DataMap();
        countryMap.putString("country", conferenceManager.getActiveConference().get().getCountry());
        putDataMapRequest.getDataMap().putDataMap(Constants.COUNTRY_PATH, countryMap);

        // Prepare and save the schedule
        ArrayList<DataMap> schedulesDataMap = new ArrayList<>();
        for (ConferenceDay day : days) {

            final DataMap scheduleDataMap = new DataMap();

            // process and push schedule's data
            String dayName = Uri.parse(day.getName()).getLastPathSegment();
            scheduleDataMap.putString("dayName", dayName);
            scheduleDataMap.putLong("dayMillis", day.getDayMs());

            schedulesDataMap.add(scheduleDataMap);
        }

       // store the list schedules
        putDataMapRequest.getDataMap().putDataMapArrayList(Constants.LIST_PATH, schedulesDataMap);

        // send the schedules
        sendToWearable(putDataMapRequest);
    }


    // Send the schedule's slots to the watch.
    private void sendSlots(Long dayMs) {

        // TODO: ensure that we have data

        List<SlotApiModel> slotApiModelList = null;

        // fetch for the slot
        final List<ConferenceDay> days = conferenceManager.getConferenceDays();
        for (ConferenceDay day : days) {
            if (day.getDayMs() == dayMs) {
                slotApiModelList = slotsDataManager.getSlotsForDay(day.getDayMs());
                break;
            }
        }

        if (slotApiModelList == null) {
            // not found
            return;
        }


        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.SLOTS_PATH + "/" + dayMs);

        // set the header (timestamp is used to force a onDataChanged event on the wearable)
        final DataMap headerMap = new DataMap();
        headerMap.putString("timestamp", new Date().toString());
        putDataMapRequest.getDataMap().putDataMap(Constants.HEADER_PATH, headerMap);

        ArrayList<DataMap> slotsDataMap = new ArrayList<>();

        for (int index = 0; index < slotApiModelList.size(); index++) {

            final DataMap scheduleDataMap = new DataMap();

            final SlotApiModel slot = slotApiModelList.get(index);

            // process the data
            scheduleDataMap.putString("roomName", slot.roomName);
            scheduleDataMap.putLong("fromTimeMillis", slot.fromTimeMillis);
            scheduleDataMap.putLong("toTimeMillis", slot.toTimeMillis);

            if (slot.isBreak()) {
                DataMap breakDataMap = new DataMap();

                //breakDataMap.putString("id", slot.getBreak().getId());
                breakDataMap.putString("nameEN", slot.slotBreak.nameEN);
                breakDataMap.putString("nameFR", slot.slotBreak.nameEN);

                scheduleDataMap.putDataMap("break", breakDataMap);
            }


            if (slot.isTalk()) {
                DataMap talkDataMap = new DataMap();

                talkDataMap.putString("id", slot.talk.id);
                talkDataMap.putBoolean("favorite", notificationsManager.isNotificationAvailable(slot.slotId));
                talkDataMap.putString("trackId", slot.talk.trackId);
                talkDataMap.putString("title", slot.talk.title);
                talkDataMap.putString("lang", slot.talk.lang);

                scheduleDataMap.putDataMap("talk", talkDataMap);
            }

            slotsDataMap.add(scheduleDataMap);
        }

        // store the list in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMapArrayList(Constants.LIST_PATH, slotsDataMap);

        // send the slots
        sendToWearable(putDataMapRequest);
    }


    private void sendTalk(String talkId) {


        SlotApiModel slotApiModel = getSlotByTalkId(talkId);
        if (slotApiModel == null) {
            return;
        }

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.TALK_PATH + "/" + talkId);

        // set the header (timestamp is used to force a onDataChanged event on the wearable)
        final DataMap headerMap = new DataMap();
        headerMap.putString("timestamp", new Date().toString());
        putDataMapRequest.getDataMap().putDataMap(Constants.HEADER_PATH, headerMap);

        final DataMap talkDataMap = new DataMap();

        // process the data
        talkDataMap.putString("timestamp", new Date().toString());
        talkDataMap.putString("id", talkId);
        talkDataMap.putBoolean("favorite", notificationsManager.isNotificationAvailable(slotApiModel.slotId));
        talkDataMap.putString("talkType", slotApiModel.talk.talkType);
        talkDataMap.putString("track", slotApiModel.talk.track);
        talkDataMap.putString("trackId", slotApiModel.talk.track);
        talkDataMap.putString("title", slotApiModel.talk.title);
        talkDataMap.putString("lang", slotApiModel.talk.lang);
        talkDataMap.putString("summary", slotApiModel.talk.summary);

        ArrayList<DataMap> speakersDataMap = new ArrayList<>();

        // process each speaker's data
        if (slotApiModel.talk.speakers != null) {


            for (int index = 0; index < slotApiModel.talk.speakers.size(); index++) {

                final TalkSpeakerApiModel speaker = slotApiModel.talk.speakers.get(index);

                final DataMap speakerDataMap = new DataMap();

                String uuid = Uri.parse(speaker.link.href).getLastPathSegment();
                speakerDataMap.putString("uuid", uuid);
                speakerDataMap.putString("name", speaker.getName());

                speakersDataMap.add(speakerDataMap);
            }
        }

        if (speakersDataMap.size() > 0) {
            talkDataMap.putDataMapArrayList(Constants.SPEAKERS_PATH, speakersDataMap);
        }

        // store the list in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMap(Constants.DETAIL_PATH, talkDataMap);

        // send the talk
        sendToWearable(putDataMapRequest);
    }

    // Send the speaker's detail to the watch.
    private void sendSpeaker(final String uuid) {


        final String id = conferenceManager.getActiveConferenceId().get();
        speakersDataManager.fetchSpeakerAsync(id, uuid,
                new AbstractDataManager.IDataManagerListener<RealmSpeaker>() {
                    @Override
                    public void onDataStartFetching() {

                    }

                    @Override
                    public void onDataAvailable(List<RealmSpeaker> items) {
                        Logger.l("Should not be there");
                    }

                    @Override
                    public void onDataAvailable(RealmSpeaker item) {
                        final RealmSpeaker speaker = speakersDataManager.getByUuid(uuid);

                        final String dataPath = Constants.CHANNEL_ID + Constants.SPEAKER_PATH + "/" + uuid;

                        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(dataPath);

                        // set the header (timestamp is used to force a onDataChanged event on the wearable)
                        final DataMap headerMap = new DataMap();
                        headerMap.putString("timestamp", new Date().toString());
                        putDataMapRequest.getDataMap().putDataMap(Constants.HEADER_PATH, headerMap);

                        final DataMap speakerDataMap = new DataMap();

                        // process the data
                        speakerDataMap.putString("uuid", uuid);
                        speakerDataMap.putString("firstName", speaker.getFirstName());
                        speakerDataMap.putString("lastName", speaker.getLastName());
                        speakerDataMap.putString("company", speaker.getCompany());
                        speakerDataMap.putString("bio", speaker.getBio());
                        speakerDataMap.putString("blog", speaker.getBlog());
                        speakerDataMap.putString("twitter", speaker.getTwitter());
                        speakerDataMap.putString("avatarURL", speaker.getAvatarURL());

                        // store the list in the datamap to send it to the wear
                        putDataMapRequest.getDataMap().putDataMap(Constants.DETAIL_PATH, speakerDataMap);

                        // send the speaker
                        sendToWearable(putDataMapRequest);

                        if ((speaker.getAvatarURL() == null) || (speaker.getAvatarURL().isEmpty())) {
                            return;
                        }

                        final ImageTarget imageTarget = new ImageTarget(dataPath, speakerDataMap);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(WearService.this)
                                        .load(speaker.getAvatarURL())
                                        .asBitmap()
                                        .centerCrop()
                                        .override(100, 100)
                                        .into(imageTarget);
                            }
                        });

                    }

                    @Override
                    public void onDataError(IOException e) {
                        if (e instanceof UnknownHostException) {
                            Logger.l("Connection error");
                        } else {
                            Logger.l("Something went wrong");
                        }
                    }
                });

        /*
        // do we have to retrieve the avatar's image?
        if (((speaker.getAvatarImage() != null) && (speaker.getAvatarImage().isEmpty() == false))) {
            return;
        }

        // retrieve and send speaker's image (if any)
        if (speaker.getAvatarURL() != null) {

            final ImageTarget imageTarget = new ImageTarget(dataPath, speakerDataMap);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Picasso.with(WearService.this)
                            .load(speaker.getAvatarURL())
                            .resize(100, 100)
                            .centerCrop()
                            .into(imageTarget);
                }
            });
        }
        */
    }


    public class ImageTarget implements Target {

        private String mDataPath;
        private DataMap mSpeakerDataMap;


        public ImageTarget(String dataPath, DataMap speakerDataMap) {
            mDataPath = dataPath;
            mSpeakerDataMap = speakerDataMap;
        }

        @Override
        public void onLoadStarted(Drawable placeholder) {

        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onResourceReady(Object resource, GlideAnimation glideAnimation) {

            // reset the existing speaker's data item
            deleteDataItem(mDataPath);

            Bitmap bitmap = (Bitmap) resource;

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // update the data map with the avatar
            mSpeakerDataMap.putString("avatarImage", encoded);

            // as the datamap has changed, a onDataChanged event will be fired on the remote node

            // store the list in the datamap to send it to the wear
            final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(mDataPath);
            putDataMapRequest.getDataMap().putDataMap(Constants.DETAIL_PATH, mSpeakerDataMap);

            // send the speaker with its profile image
            sendToWearable(putDataMapRequest);

        }

        @Override
        public void onLoadCleared(Drawable placeholder) {

        }

        @Override
        public void getSize(SizeReadyCallback cb) {

        }

        @Override
        public void setRequest(Request request) {

        }

        @Override
        public Request getRequest() {
            return null;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onDestroy() {

        }
    }


    // send Favorite to the watch
    private void sendFavorite(String talkId) {

        SlotApiModel slotApiModel = getSlotByTalkId(talkId);
        if (slotApiModel == null) {
            return;
        }

        // send the event
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.FAVORITE_PATH + "/" + talkId);

        // set the header (timestamp is used to force a onDataChanged event on the wearable)
        final DataMap headerMap = new DataMap();
        headerMap.putString("timestamp", new Date().toString());
        putDataMapRequest.getDataMap().putDataMap(Constants.HEADER_PATH, headerMap);

        // store the data
        DataMap dataMap = new DataMap();
        dataMap.putBoolean("favorite", notificationsManager.isNotificationAvailable(slotApiModel.slotId));

        // store the event in the datamap to send it to the wear
        putDataMapRequest.getDataMap().putDataMap(Constants.DETAIL_PATH, dataMap);

        // send the favorite's status to the watch
        sendToWearable(putDataMapRequest);
    }

    // remove the favorite from the talk
    private void removeFavorite(String talkId) {

        SlotApiModel slotApiModel = getSlotByTalkId(talkId);
        if (slotApiModel == null) {
            return;
        }

        notificationsManager.removeNotification(slotApiModel.slotId);

        EventBus.getDefault().postLocal(new ScheduleEvent());

        sendFavorite(talkId);
    }

    // add the favorite to the talk
    private void addFavorite(String talkId) {

        SlotApiModel slotApiModel = getSlotByTalkId(talkId);
        if (slotApiModel == null) {
            return;
        }

        notificationsManager.scheduleNotification(slotApiModel, false);

        EventBus.getDefault().postLocal(new ScheduleEvent());

        sendFavorite(talkId);
    }




    private SlotApiModel getSlotByTalkId(String talkId) {

        final String confId = conferenceManager.getActiveConferenceId().get();
        try {
            speakersDataManager.fetchSpeakersSync(confId);
        } catch (IOException e) {
            return null;
        }

        if (slotsDataManager == null) {
            return null;
        }

        final Optional<SlotApiModel> opt = slotsDataManager.getSlotByTalkId(talkId);
        if (!opt.isPresent()) {
            return null;
        }
        SlotApiModel slotApiModel = opt.get();

        if (!slotApiModel.isTalk()) {
            // not a talk
            return null;
        }

        return slotApiModel;
    }

        @Override
    public void onConnected(Bundle bundle) {

        //deleteDataItem(Constants.CHANNEL_ID);

    }


    private void deleteDataItem(String path) {

        Uri uri = new Uri.Builder()
                .scheme(PutDataRequest.WEAR_URI_SCHEME)
                .path(path)
                .build();

        Wearable.DataApi.deleteDataItems(mApiClient, uri, DataApi.FILTER_PREFIX);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
