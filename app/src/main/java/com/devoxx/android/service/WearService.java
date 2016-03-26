package com.devoxx.android.service;

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
import com.devoxx.data.manager.SlotsDataManager;
import com.devoxx.data.manager.SpeakersDataManager;
import com.devoxx.data.model.RealmSpeaker;
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

@EService
public class WearService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks {

    private final static String TAG = WearService.class.getCanonicalName();

    @Bean
    ConferenceManager conferenceManager;

    @Bean
    SlotsDataManager slotsDataManager;

    @Bean
    SpeakersDataManager speakersDataManager;

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



        /*
        super.onMessageReceived(messageEvent);

        Log.d(TAG, "onMessageReceived");

        //Ouvre une connexion vers la montre
        ConnectionResult connectionResult = mApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }

        //traite le message reçu
        final String path = messageEvent.getPath();

        if(path.equalsIgnoreCase("talk")) {

            final String talkDay = new String(messageEvent.getData());

            //final List<SlotApiModel> slotApiModelList = slotsDataManager.getLastTalks();

            final List<ConferenceDay> days = conferenceManager.getConferenceDays();
            for (ConferenceDay day : days) {
                if (day.getName().equalsIgnoreCase(talkDay)) {
                    final List<SlotApiModel> slotApiModelList = slotsDataManager.getSlotsForDay(day.getDayMs());
                    Log.d(TAG, "Count = " + slotApiModelList.size());
                    break;
                }
                Log.d(TAG, day.getName());
            }

            int random = (int)(Math.random() * 100);
            Log.d(TAG, "random number:" + random);
        }
        */
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


    private void sendSchedules() {

        // TODO: ensure that we have data

        final List<ConferenceDay> days = conferenceManager.getConferenceDays();

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.SCHEDULES_PATH);


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

        final String confId = conferenceManager.getActiveConferenceId().get();
        try {
            speakersDataManager.fetchSpeakersSync(confId);
        } catch (IOException e) {
            return;
        }

        if (slotsDataManager == null) {
            return;
        }

        final Optional<SlotApiModel> opt = slotsDataManager.getSlotByTalkId(talkId);
        if (!opt.isPresent()) {
            return;
        }
        SlotApiModel slotApiModel = opt.get();

        if (!slotApiModel.isTalk()) {
            // not a talk
            return;
        }

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID + Constants.TALK_PATH + "/" + talkId);

        final DataMap talkDataMap = new DataMap();

        // process the data
        talkDataMap.putString("timestamp", new Date().toString());
        talkDataMap.putString("id", talkId);
        //talkDataMap.putLong("eventId", talk.getEventId());
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


                /*

                RealmSpeaker realmSpeaker = speakersDataManager.getByUuid(uuid);
                if (realmSpeaker != null) {
                    final DataMap speakerDataMap = new DataMap();

                    speakerDataMap.putString("uuid", uuid);
                    speakerDataMap.putString("name", realmSpeaker.getFirstName() + " " + realmSpeaker.getLastName());

                    speakersDataMap.add(speakerDataMap);
                }
                */

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

                        final DataMap speakerDataMap = new DataMap();

                        // process the data
                        speakerDataMap.putString("timestamp", new Date().toString());
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

            // reset the existing data item
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


    @Override
    public void onConnected(Bundle bundle) {

        deleteDataItem(Constants.CHANNEL_ID);

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
