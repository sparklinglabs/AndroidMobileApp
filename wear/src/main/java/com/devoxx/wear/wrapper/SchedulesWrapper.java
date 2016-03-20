package com.devoxx.wear.wrapper;

import com.devoxx.common.utils.Constants;
import com.devoxx.wear.model.Schedule;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eloudsa on 03/09/15.
 */
public class SchedulesWrapper {

    public List<Schedule> getSchedulesList(DataEvent dataEvent) {

        List<Schedule> schedulesList = new ArrayList<>();

        if (dataEvent == null) {
            return schedulesList;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return schedulesList;
        }


        return getSchedulesList(dataMapItem.getDataMap());
    }


    public List<Schedule> getSchedulesList(DataMap dataMap) {

        List<Schedule> schedulesList = new ArrayList<>();

        if (dataMap == null) {
            return schedulesList;
        }

        List<DataMap> schedulesDataMap = dataMap.getDataMapArrayList(Constants.LIST_PATH);
        if (schedulesDataMap == null) {
            return schedulesList;
        }

        for (DataMap scheduleDataMap : schedulesDataMap) {
            // retrieve the speaker's information

            schedulesList.add(new Schedule(
                    scheduleDataMap.getString("dayName"),
                    scheduleDataMap.getLong("dayMillis")));
        }

        return schedulesList;

    }


    public String getCountry(DataEvent dataEvent) {


        if (dataEvent == null) {
            return null;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return null;
        }

        return getCountry(dataMapItem.getDataMap());
    }


    public String getCountry(DataMap dataMap) {

        if (dataMap == null) {
            return null;
        }

        DataMap countryMap = dataMap.getDataMap(Constants.COUNTRY_PATH);
        if (countryMap == null) {
            return null;
        }

        return countryMap.getString("country");
    }

}
