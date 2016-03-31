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

        talk.setId(dataTalkMap.getString(Constants.DATAMAP_ID, ""));
        talk.setFavorite(dataTalkMap.getBoolean(Constants.DATAMAP_FAVORITE, false));
        talk.setTalkType(dataTalkMap.getString(Constants.DATAMAP_TALK_TYPE, ""));
        talk.setTrack(dataTalkMap.getString(Constants.DATAMAP_TRACK, ""));
        talk.setTrackId(dataTalkMap.getString(Constants.DATAMAP_TRACK_ID, ""));
        talk.setTitle(dataTalkMap.getString(Constants.DATAMAP_TITLE, ""));
        talk.setLang(dataTalkMap.getString(Constants.DATAMAP_LANG, ""));
        talk.setSummary(dataTalkMap.getString(Constants.DATAMAP_SUMMARY, ""));


        List<DataMap> speakersDataMap = dataTalkMap.getDataMapArrayList(Constants.SPEAKERS_PATH);
        if (speakersDataMap == null) {
            return talk;
        }

        for (DataMap speakerDataMap : speakersDataMap) {
            // retrieve the speaker's information

            TalkSpeakerApiModel speaker = new TalkSpeakerApiModel();

            speaker.setUuid(speakerDataMap.getString(Constants.DATAMAP_UUID, ""));
            speaker.setName(speakerDataMap.getString(Constants.DATAMAP_NAME, ""));

            talk.addSpeaker(speaker);
        }

        return talk;
    }

}
