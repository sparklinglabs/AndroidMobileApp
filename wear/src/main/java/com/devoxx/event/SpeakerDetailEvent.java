package com.devoxx.event;


import com.devoxx.model.TalkSpeakerApiModel;

/**
 * Created by eloudsa on 30/08/15.
 */
public class SpeakerDetailEvent {

    private TalkSpeakerApiModel speaker;


    public SpeakerDetailEvent(TalkSpeakerApiModel speaker) {
        this.speaker = speaker;
    }

    public TalkSpeakerApiModel getSpeaker() {
        return speaker;
    }

    public void setSpeaker(TalkSpeakerApiModel speaker) {
        this.speaker = speaker;
    }
}
