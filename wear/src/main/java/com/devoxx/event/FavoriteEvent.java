package com.devoxx.event;

/**
 * Created by eloudsa on 03/11/15.
 */
public class FavoriteEvent {

    private boolean favorite;

    public FavoriteEvent(boolean favorite) {
        this.favorite = favorite;
    }

    public boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
