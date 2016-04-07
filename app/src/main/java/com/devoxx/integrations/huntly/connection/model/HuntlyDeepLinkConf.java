package com.devoxx.integrations.huntly.connection.model;

import com.devoxx.integrations.huntly.storage.RealmHuntlyDeepLinks;

import java.io.Serializable;

public class HuntlyDeepLinkConf implements Serializable {
	private String deepLink;
	private String playStoreUri;
	private String appStoreUri;

	public static HuntlyDeepLinkConf fromDb(RealmHuntlyDeepLinks links) {
		final HuntlyDeepLinkConf result = new HuntlyDeepLinkConf();
		result.deepLink = links.getDeepLink();
		result.appStoreUri = links.getAppStoreUri();
		result.playStoreUri = links.getPlayStoreUri();
		return result;
	}

	public String getDeepLink() {
		return deepLink;
	}

	public String getPlayStoreUri() {
		return playStoreUri;
	}

	public String getAppStoreUri() {
		return appStoreUri;
	}
}
