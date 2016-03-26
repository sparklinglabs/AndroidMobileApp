package com.devoxx.wear.wrapper;

import com.devoxx.common.utils.Constants;
import com.devoxx.model.TalkSpeakerApiModel;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;


/**
 * Created by eloudsa on 29/08/15.
 */
public class SpeakerDetailWrapper {


    public TalkSpeakerApiModel getSpeakerDetail(DataEvent dataEvent) {


        if (dataEvent == null) {
            return null;
        }

        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
        if (dataMapItem == null) {
            return null;
        }


        return getSpeakerDetail(dataMapItem.getDataMap());
    }

    public TalkSpeakerApiModel getSpeakerDetail(DataMap dataMap) {

        DataMap speakerDataMap = dataMap.getDataMap(Constants.DETAIL_PATH);
        if (speakerDataMap == null) {
            return null;
        }

        // retrieve the speaker's information
        TalkSpeakerApiModel speaker = new TalkSpeakerApiModel();
        speaker.setUuid(speakerDataMap.getString("uuid"));
        speaker.setFirstName(speakerDataMap.getString("firstName"));
        speaker.setLastName(speakerDataMap.getString("lastName"));
        speaker.setTwitter(speakerDataMap.getString("twitter"));
        speaker.setAvatarURL(speakerDataMap.getString("avatarURL"));
        speaker.setAvatarImage(speakerDataMap.getString("avatarImage"));

        return speaker;


    }

}
