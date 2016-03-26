package com.devoxx.android.service;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.annimon.stream.Optional;
import com.devoxx.common.utils.Constants;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.connection.model.TalkSpeakerApiModel;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.conference.model.ConferenceDay;
import com.devoxx.data.manager.SlotsDataManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@EService
public class WearService extends WearableListenerService {

    private final static String TAG = WearService.class.getCanonicalName();

    @Bean
    ConferenceManager conferenceManager;

    @Bean
    SlotsDataManager slotsDataManager;


    protected GoogleApiClient mApiClient;


    @Override
    public void onDestroy() {
        super.onDestroy();
        if ((mApiClient != null) && (mApiClient.isConnected())) {
            mApiClient.disconnect();
        }
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

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(Constants.CHANNEL_ID +Constants.TALK_PATH + "/" + talkId);

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

                final DataMap speakerDataMap = new DataMap();

                final TalkSpeakerApiModel speaker = slotApiModel.talk.speakers.get(index);

                speakerDataMap.putString("uuid", speaker.uuid);
                speakerDataMap.putString("uuid", speaker.name);

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

}
