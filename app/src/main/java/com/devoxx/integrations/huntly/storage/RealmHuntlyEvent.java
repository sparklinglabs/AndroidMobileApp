package com.devoxx.integrations.huntly.storage;

import com.devoxx.integrations.huntly.connection.model.HuntlyEvent;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmHuntlyEvent extends RealmObject {
	@PrimaryKey private String externalId;
	private long id;

	public static RealmHuntlyEvent fromApi(HuntlyEvent event) {
		final RealmHuntlyEvent result = new RealmHuntlyEvent();
		result.setExternalId(event.getExternalId());
		result.setId(event.getEventId());
		return result;
	}

	public long getId() {
		return id;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
}
