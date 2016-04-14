package com.devoxx.data.user;

import com.devoxx.data.RealmProvider;
import com.devoxx.data.model.RealmFavouriteTalk;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import io.realm.Realm;

@EBean
public class UserFavouritedTalksManager {

	@Bean RealmProvider realmProvider;

	public boolean isFavouriteTalk(String talkId) {
		final Realm realm = realmProvider.getRealm();
		final RealmFavouriteTalk favouriteTalk = realm.where(RealmFavouriteTalk.class)
				.equalTo("talkId", talkId).findFirst();
		final boolean result = favouriteTalk != null;
		realm.close();
		return result;
	}

	public void favouriteTalk(String talkId) {
		if (!isFavouriteTalk(talkId)) {
			final Realm realm = realmProvider.getRealm();
			realm.beginTransaction();
			final RealmFavouriteTalk favouriteTalk = realm.createObject(RealmFavouriteTalk.class);
			favouriteTalk.setTalkId(talkId);
			realm.commitTransaction();
			realm.close();
		}
	}

	public void unFavouriteTalk(String talkId) {
		if (isFavouriteTalk(talkId)) {
			final Realm realm = realmProvider.getRealm();
			realm.beginTransaction();
			realm.where(RealmFavouriteTalk.class).equalTo("talkId", talkId).findAll().clear();
			realm.commitTransaction();
			realm.close();
		}
	}
}
