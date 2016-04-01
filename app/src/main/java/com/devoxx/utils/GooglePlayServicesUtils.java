package com.devoxx.utils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import android.app.Activity;
import android.widget.Toast;

public class GooglePlayServicesUtils {

	private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 12;

	public static boolean requestGooglePlayServices(Activity activity) {
		final GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
		final int status = apiAvailability.isGooglePlayServicesAvailable(activity);
		final boolean result = status == ConnectionResult.SUCCESS;
		if (status != ConnectionResult.SUCCESS) {
			if (apiAvailability.isUserResolvableError(status)) {
				apiAvailability.getErrorDialog(activity, status, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
			} else {
				Toast.makeText(activity, "This device is not supported.", Toast.LENGTH_LONG).show();
			}
		}
		return result;
	}
}
