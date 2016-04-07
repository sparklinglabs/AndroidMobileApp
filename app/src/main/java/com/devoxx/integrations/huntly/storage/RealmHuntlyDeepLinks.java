package com.devoxx.integrations.huntly.storage;

import com.devoxx.integrations.huntly.connection.model.HuntlyDeepLinkConf;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmHuntlyDeepLinks extends RealmObject {
	@PrimaryKey private String playStoreUri;
	private String confId;
	private String deepLink;
	private String appStoreUri;

	public static RealmHuntlyDeepLinks fromApi(HuntlyDeepLinkConf conf, String confId) {
		final RealmHuntlyDeepLinks result = new RealmHuntlyDeepLinks();
		result.setAppStoreUri(conf.getAppStoreUri());
		result.setDeepLink(conf.getDeepLink());
		result.setPlayStoreUri(conf.getPlayStoreUri());
		result.setConfId(confId);
		return result;
	}

	public String getDeepLink() {
		return deepLink;
	}

	public void setDeepLink(String deepLink) {
		this.deepLink = deepLink;
	}

	public String getPlayStoreUri() {
		return playStoreUri;
	}

	public void setPlayStoreUri(String playStoreUri) {
		this.playStoreUri = playStoreUri;
	}

	public String getAppStoreUri() {
		return appStoreUri;
	}

	public void setAppStoreUri(String appStoreUri) {
		this.appStoreUri = appStoreUri;
	}

	public String getConfId() {
		return confId;
	}

	public void setConfId(String confId) {
		this.confId = confId;
	}
}
