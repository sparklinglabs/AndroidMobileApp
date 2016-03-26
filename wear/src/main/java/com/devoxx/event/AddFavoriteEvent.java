package com.devoxx.event;


import com.devoxx.model.TalkFullApiModel;

/**
 * Created by eloudsa on 20/09/15.
 */
public class AddFavoriteEvent {

    private TalkFullApiModel talk;

    public AddFavoriteEvent(TalkFullApiModel talkCalendar) {
        this.talk = talkCalendar;
    }

    public TalkFullApiModel getTalk() {
        return talk;
    }
}
