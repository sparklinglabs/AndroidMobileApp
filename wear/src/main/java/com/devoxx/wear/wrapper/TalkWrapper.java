package com.devoxx.wear.wrapper;

import com.devoxx.common.utils.Constants;
import com.devoxx.model.TalkFullApiModel;
import com.devoxx.model.TalkSpeakerApiModel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

import java.util.List;

/**
 * Created by eloudsa on 08/09/15.
 */
public class TalkWrapper {

    public TalkFullApiModel getTalk(DataEvent dataEvent) {


        if (dataEvent == null) {
            return null;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return null;
        }

        return getTalk(dataMapItem.getDataMap());
    }


    public TalkFullApiModel getTalk(DataMap dataMap) {

        if (dataMap == null) {
            return null;
        }

        DataMap dataTalkMap = dataMap.getDataMap(Constants.DETAIL_PATH);
        if (dataTalkMap == null) {
            return null;
        }

        TalkFullApiModel talk = new TalkFullApiModel();

        talk.setId(dataTalkMap.getString("id"));
        talk.setTalkType(dataTalkMap.getString("talkType"));
        talk.setTrack(dataTalkMap.getString("track"));
        talk.setTrackId(dataTalkMap.getString("trackId"));
        talk.setTitle(dataTalkMap.getString("title"));
        talk.setLang(dataTalkMap.getString("lang"));
        talk.setSummary(dataTalkMap.getString("summary"));


        List<DataMap> speakersDataMap = dataTalkMap.getDataMapArrayList(Constants.SPEAKERS_PATH);
        if (speakersDataMap == null) {
            return talk;
        }

        for (DataMap speakerDataMap : speakersDataMap) {
            // retrieve the speaker's information

            TalkSpeakerApiModel speaker = new TalkSpeakerApiModel();

            speaker.setUuid(speakerDataMap.getString("uuid"));
            speaker.setName(speakerDataMap.getString("name"));


            talk.addSpeaker(speaker);
        }

        return talk;
    }

}
