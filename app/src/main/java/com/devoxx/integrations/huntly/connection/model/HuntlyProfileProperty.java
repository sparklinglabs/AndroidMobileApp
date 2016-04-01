package com.devoxx.integrations.huntly.connection.model;

import java.io.Serializable;

public class HuntlyProfileProperty implements Serializable {
	private String key;
	private String value;

	public static HuntlyProfileProperty create(String k, String v) {
		final HuntlyProfileProperty result = new HuntlyProfileProperty();
		result.key = k;
		result.value = v;
		return result;
	}
}
