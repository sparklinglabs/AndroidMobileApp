package com.devoxx.integrations.huntly.connection.model;

import java.io.Serializable;

public class HuntlyEvent implements Serializable {
	private String externalId;
	private long eventId;

	public String getExternalId() {
		return externalId;
	}

	public long getEventId() {
		return eventId;
	}
}
