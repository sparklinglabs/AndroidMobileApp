package com.devoxx.data;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import android.content.Context;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

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
						.schemaVersion(2)
						.migration(new SchemaMigration())
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

	private static class SchemaMigration implements RealmMigration {

		@Override public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
			final RealmSchema schema = realm.getSchema();
			if (oldVersion == 1) {
				schema.get("RealmNotification")
						.addField("talkEndTime", long.class);

				schema.create("RealmFavouriteTalk")
						.addField("talkId", String.class, FieldAttribute.PRIMARY_KEY);
				oldVersion++;
			}
		}
	}
}
