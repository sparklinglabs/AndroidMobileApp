package com.devoxx.data;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

@EBean(scope = EBean.Scope.Singleton)
public class RealmProvider {

	private static final String DATABASE_NAME = "devoxx_db";

	@RootContext
	Context context;

	private boolean inited = false;

	public void init() {
		final RealmConfiguration configuration =
				new RealmConfiguration.Builder(context)
						.name(DATABASE_NAME)
						.schemaVersion(1)
						.build();
		Realm.setDefaultConfiguration(configuration);

		inited = true;
	}

	public Realm getRealm() {
		if (!inited) {
			init();
		}

		return Realm.getDefaultInstance();
	}
}
