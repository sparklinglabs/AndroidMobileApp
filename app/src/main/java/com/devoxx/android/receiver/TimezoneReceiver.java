package com.devoxx.android.receiver;

import com.devoxx.data.manager.NotificationsManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@EReceiver
public class TimezoneReceiver extends BroadcastReceiver {

	@Bean
	NotificationsManager naNotificationsManager;

	public TimezoneReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		naNotificationsManager.resetAlarms();
	}
}
