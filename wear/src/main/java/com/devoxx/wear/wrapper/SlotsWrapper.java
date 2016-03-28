package com.devoxx.wear.wrapper;


import com.devoxx.common.utils.Constants;
import com.devoxx.model.BreakApiModel;
import com.devoxx.model.SlotApiModel;
import com.devoxx.model.TalkFullApiModel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eloudsa on 03/09/15.
 */
public class SlotsWrapper {

    public List<SlotApiModel> getSlotsList(DataEvent dataEvent) {

        List<SlotApiModel> slotsList = new ArrayList<>();

        if (dataEvent == null) {
            return slotsList;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return slotsList;
        }

        return getSlotsList(dataMapItem.getDataMap());
    }



    public List<SlotApiModel> getSlotsList(DataMap dataMap) {

        List<SlotApiModel> slotsList = new ArrayList<>();

        if (dataMap == null) {
            return slotsList;
        }

        List<DataMap> slotsDataMap = dataMap.getDataMapArrayList(Constants.LIST_PATH);
        if (slotsDataMap == null) {
            return slotsList;
        }

        for (DataMap slotDataMap : slotsDataMap) {
            // retrieve the speaker's information

            SlotApiModel slot = new SlotApiModel();


            slot.setSlotId(slotDataMap.getString("slotId"));
            slot.setRoomName(slotDataMap.getString("roomName"));
            slot.setFromTimeMillis(slotDataMap.getLong("fromTimeMillis"));
            slot.setToTimeMillis(slotDataMap.getLong("toTimeMillis"));


            DataMap breakDataMap = slotDataMap.getDataMap("break");
            if (breakDataMap != null) {
                BreakApiModel breakSlot = new BreakApiModel();

                breakSlot.setId(breakDataMap.getString("id"));
                breakSlot.setNameEN(breakDataMap.getString("nameEN"));
                breakSlot.setNameFR(breakDataMap.getString("nameFR"));

                slot.setSlotBreak(breakSlot);
            }

            DataMap talkDataMap = slotDataMap.getDataMap("talk");
            if (talkDataMap != null) {
                TalkFullApiModel talkSlot = new TalkFullApiModel();

                talkSlot.setId(talkDataMap.getString("id"));
                talkSlot.setFavorite(talkDataMap.getBoolean("favorite"));
                talkSlot.setLang(talkDataMap.getString("lang"));
                talkSlot.setSummary(talkDataMap.getString("summary"));
                talkSlot.setTalkType(talkDataMap.getString("talkType"));
                talkSlot.setTitle(talkDataMap.getString("title"));
                talkSlot.setTrack(talkDataMap.getString("track"));
                talkSlot.setTrackId(talkDataMap.getString("trackId"));

                slot.setTalk(talkSlot);
            }

            // skip unknown talks
            if ((slot.getSlotBreak() != null) || (slot.getTalk() != null)) {
                slotsList.add(slot);
            }


        }

        return slotsList;

    }

}
