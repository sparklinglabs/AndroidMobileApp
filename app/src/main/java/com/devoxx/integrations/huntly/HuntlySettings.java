package com.devoxx.integrations.huntly;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface HuntlySettings {
	@DefaultString("") String token();

	@DefaultLong(-1) long userId();

	@DefaultBoolean(true) boolean isFirstRun();

	@DefaultLong(HuntlyController.UNKNOWN_EVENT_ID) long eventId();

	@DefaultString("") String promo();
}
