package com.devoxx;

import com.crashlytics.android.Crashlytics;
import com.devoxx.data.Settings_;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.integrations.IntegrationProvider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Application;

import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;

@EApplication
public class MainApplication extends Application {

	@Bean IntegrationProvider integrationProvider;
	@Bean ConferenceManager conferenceManager;

	@Pref Settings_ settings;

	@Override
	public void onCreate() {
		super.onCreate();

		if (settings.isFirstStart().getOr(true)) {
			settings.edit().isFirstStart().put(false).apply();
			conferenceManager.initWitStaticData();
		}

		integrationProvider.provideIntegrationController().init();
		Fabric.with(this, new Crashlytics());
		JodaTimeAndroid.init(this);
	}
}
