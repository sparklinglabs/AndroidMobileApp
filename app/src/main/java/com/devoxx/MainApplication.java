package com.devoxx;

import com.crashlytics.android.Crashlytics;
import com.devoxx.integrations.IntegrationProvider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import android.app.Application;

import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;

@EApplication
public class MainApplication extends Application {

	@Bean IntegrationProvider integrationProvider;

	@Override
	public void onCreate() {
		super.onCreate();

		integrationProvider.provideIntegrationController().init();
		Fabric.with(this, new Crashlytics());
		JodaTimeAndroid.init(this);
	}
}
