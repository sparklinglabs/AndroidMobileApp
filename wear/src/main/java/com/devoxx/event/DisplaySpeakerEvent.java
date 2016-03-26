package com.devoxx.event;

import com.devoxx.model.TalkSpeakerApiModel;

/**
 * Created by eloudsa on 13/09/15.
 */
public class DisplaySpeakerEvent {


    private String pageName;
    private TalkSpeakerApiModel speaker;


    public DisplaySpeakerEvent(TalkSpeakerApiModel speaker) {
        this.speaker = speaker;
    }

    public DisplaySpeakerEvent(String pageName, TalkSpeakerApiModel speaker) {
        this.pageName = pageName;
        this.speaker = speaker;
    }

    public TalkSpeakerApiModel getSpeaker() {
        return speaker;
    }

    public void setSpeaker(TalkSpeakerApiModel speaker) {
        this.speaker = speaker;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
}
