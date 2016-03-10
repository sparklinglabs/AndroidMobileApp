package com.devoxx;

import com.crashlytics.android.Crashlytics;

import org.androidannotations.annotations.EApplication;

import android.app.Application;

import io.fabric.sdk.android.Fabric;
import net.danlew.android.joda.JodaTimeAndroid;

@EApplication
public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
		JodaTimeAndroid.init(this);
	}
}
