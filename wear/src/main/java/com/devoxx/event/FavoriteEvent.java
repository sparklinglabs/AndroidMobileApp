package com.devoxx.event;

/**
 * Created by eloudsa on 03/11/15.
 */
public class FavoriteEvent {

    private Boolean favorite;

    public FavoriteEvent(Boolean favorite) {
        this.favorite = favorite;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
