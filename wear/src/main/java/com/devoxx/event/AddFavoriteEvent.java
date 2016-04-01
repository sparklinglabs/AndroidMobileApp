package com.devoxx.event;


/**
 * Created by eloudsa on 20/09/15.
 */
public class AddFavoriteEvent {

    private String talkId;


    public AddFavoriteEvent(String talkId) {
        this.talkId = talkId;
    }

    public String getTalkId() {
        return talkId;
    }

    public void setTalkId(String talkId) {
        this.talkId = talkId;
    }
}
