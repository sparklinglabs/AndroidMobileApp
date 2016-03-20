package com.devoxx.android.service;

import android.os.Bundle;

import com.devoxx.common.utils.Constants;
import com.devoxx.common.utils.Utils;
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
import java.util.List;

@EService
public class WearService extends WearableListenerService {

    private final static String TAG = WearService.class.getCanonicalName();

    @Bean
    SlotsDataManager slotsDataManager;

    @Bean
    ConferenceManager conferenceManager;

    protected GoogleApiClient mApiClient;


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mApiClient.isConnected()) {
            mApiClient.disconnect();
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Processing the incoming message
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());


        // request for schedules
        if (path.startsWith(Constants.CHANNEL_ID + Constants.SCHEDULES_PATH)) {
            sendSchedules();
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


    private void sendSchedules() {

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
            scheduleDataMap.putString("dayName", Utils.getLastPartUrl(day.getName()));
            scheduleDataMap.putLong("dayMillis", day.getDayMs());

            schedulesDataMap.add(scheduleDataMap);
        }

       // store the list schedules
        putDataMapRequest.getDataMap().putDataMapArrayList(Constants.LIST_PATH, schedulesDataMap);

        /*
        // send the schedules
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
        */

        // send the schedules
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
}
