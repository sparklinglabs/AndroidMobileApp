package com.devoxx.android.service;

import android.os.Bundle;

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


    private static final String CHANNEL_ID = "/000000";
    private static final String SCHEDULES_PATH = "/schedules";
    private static final String LIST_PATH = "/list";


    @Override
    public void onCreate() {
        super.onCreate();
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        // Processing the incoming message
        String path = messageEvent.getPath();
        String data = new String(messageEvent.getData());


        if (path.startsWith(CHANNEL_ID + SCHEDULES_PATH)) {
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

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(CHANNEL_ID + SCHEDULES_PATH);

        ArrayList<DataMap> schedulesDataMap = new ArrayList<>();

        for (ConferenceDay day : days) {

            final DataMap scheduleDataMap = new DataMap();

            // process and push schedule's data
            scheduleDataMap.putString("day", getLastPartUrl(day.getName()));
            scheduleDataMap.putString("title", day.getName());

            schedulesDataMap.add(scheduleDataMap);
        }

       // store the list in the datamap to send it to the watch
        putDataMapRequest.getDataMap().putDataMapArrayList(LIST_PATH, schedulesDataMap);

        /*
        // send the schedules
        if (mApiClient.isConnected()) {
            Wearable.DataApi.putDataItem(mApiClient, putDataMapRequest.asPutDataRequest());
        }
        */

        // send the speaker
        // event not more defined on the calendar -> inform the watch
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


    private static String getLastPartUrl(String url) {

        if (url == null) {
            return "";
        }

        String href = url.trim();
        if (href == "") {
            return "";
        }


        if (href.endsWith("/")) {
            // remove last trailing slash
            href = href.substring(0, href.length()-1);
        }

        return(href.substring(href.lastIndexOf("/") + 1,href.length()));
    }
}
