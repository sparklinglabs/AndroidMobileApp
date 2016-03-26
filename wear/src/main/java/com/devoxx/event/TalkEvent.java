package com.devoxx.event;


import com.devoxx.model.TalkFullApiModel;

/**
 * Created by eloudsa on 08/09/15.
 */
public class TalkEvent {

    private TalkFullApiModel talk;


    public TalkEvent(TalkFullApiModel talk) {
        this.talk = talk;
    }

    public TalkFullApiModel getTalk() {
        return talk;
    }

    public void setTalk(TalkFullApiModel talk) {
        this.talk = talk;
    }
}
