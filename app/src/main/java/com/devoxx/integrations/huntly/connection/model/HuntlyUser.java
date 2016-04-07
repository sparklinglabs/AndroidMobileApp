package com.devoxx.integrations.huntly.connection.model;

import java.io.Serializable;

public class HuntlyUser implements Serializable {
	private String username;
	private String token;
	private long userId;

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public long getUserId() {
		return userId;
	}
}
