package com.devoxx.data.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmFavouriteTalk extends RealmObject {
	@PrimaryKey private String talkId;

	public String getTalkId() {
		return talkId;
	}

	public void setTalkId(String talkId) {
		this.talkId = talkId;
	}
}
