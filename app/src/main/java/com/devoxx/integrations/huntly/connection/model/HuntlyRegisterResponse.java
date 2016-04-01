package com.devoxx.integrations.huntly.connection.model;

import java.io.Serializable;

public class HuntlyRegisterResponse implements Serializable {
	private String status;
	private HuntlyUser user;
	private boolean firstLogin;

	public String getStatus() {
		return status;
	}

	public HuntlyUser getUser() {
		return user;
	}

	public boolean isFirstLogin() {
		return firstLogin;
	}
}
