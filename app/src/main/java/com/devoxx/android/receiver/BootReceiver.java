package com.devoxx.android.receiver;

import com.devoxx.data.manager.NotificationsManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

@EReceiver
public class BootReceiver extends BroadcastReceiver {

	@Bean
	NotificationsManager notificationsManager;

	public BootReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		notificationsManager.resetAlarms();
	}
}
