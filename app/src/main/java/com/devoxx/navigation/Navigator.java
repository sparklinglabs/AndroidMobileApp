package com.devoxx.navigation;

import com.devoxx.R;
import com.devoxx.android.activity.MainActivity;
import com.devoxx.android.activity.SpeakerDetailsHostActivity_;
import com.devoxx.android.activity.TalkDetailsHostActivity_;
import com.devoxx.android.fragment.map.MapMainFragment_;
import com.devoxx.android.fragment.map.MapMenuLandscapeFragment_;
import com.devoxx.android.fragment.speaker.SpeakerFragment_;
import com.devoxx.android.fragment.talk.TalkFragment_;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.utils.DeviceUtil;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.Fragment;

@EBean
public class Navigator {

	@Bean
	DeviceUtil deviceUtil;

	@Pref
	NavigationHelper_ navigationHelper;

	public void setUpdateNeeded() {
		navigationHelper.edit().isUpdateNeeded().put(true).apply();
	}

	private void clearUpdateNeeded() {
		navigationHelper.edit().isUpdateNeeded().put(false).apply();
	}

	public boolean isUpdateNeeded() {
		final boolean result = navigationHelper.isUpdateNeeded().get();
		if (result) {
			clearUpdateNeeded();
		}
		return result;
	}

	public void openTalkDetails(
			MainActivity mainActivity, SlotApiModel slotApiModel, Fragment fragment, boolean notifyAboutChange) {
		if (deviceUtil.isLandscapeTablet()) {
			mainActivity.replaceFragmentInGivenContainer(
					TalkFragment_.builder().slotApiModel(slotApiModel)
							.notifyAboutChange(notifyAboutChange).build(),
					false, R.id.content_frame_second);
		} else {
			TalkDetailsHostActivity_.intent(fragment).
					slotApiModel(slotApiModel).start();
		}
	}

	public void openTalkDetails(Activity mainActivity, SlotApiModel slotApiModel, boolean notifyAboutChange) {
		if (deviceUtil.isLandscapeTablet()) {
			((MainActivity) mainActivity).replaceFragmentInGivenContainer(
					TalkFragment_.builder().slotApiModel(slotApiModel)
							.notifyAboutChange(notifyAboutChange).build(),
					false, R.id.content_frame_second);
		} else {
			TalkDetailsHostActivity_.intent(mainActivity).
					slotApiModel(slotApiModel).start();
		}
	}

	public void openSpeakerDetails(Activity mainActivity, String speakerUuid) {
		if (deviceUtil.isLandscapeTablet()) {
			((MainActivity) mainActivity).replaceFragmentInGivenContainer(
					SpeakerFragment_.builder().speakerUuid(speakerUuid).build(),
					false, R.id.content_frame_second);
		} else {
			SpeakerDetailsHostActivity_.intent(mainActivity).speakerUuid(speakerUuid).start();
		}
	}

	public void openMaps(MainActivity mainActivity) {
		if (deviceUtil.isLandscapeTablet()) {
			mainActivity.replaceFragmentInGivenContainer(
					MapMenuLandscapeFragment_.builder().build(), false, R.id.content_frame);
		} else {
			mainActivity.replaceFragmentInGivenContainer(
					MapMainFragment_.builder().build(), false, R.id.content_frame);
		}
	}

	public void openWwwLink(Activity activity, String www) {
		final String finalUrl =
				(!www.startsWith("http://") && !www.startsWith("https://")) ? "http://" + www : www;
		activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl)));
	}

	public void openTwitterUser(Activity activity, String twitterName) {
		String formattedTwitterAddress = "http://twitter.com/" + twitterName.replace("@", "");
		Intent browseTwitter = new Intent(Intent.ACTION_VIEW, Uri.parse(formattedTwitterAddress));
		activity.startActivity(browseTwitter);
	}

	public void tweetMessage(Activity activity, String twitterMessage) {
		final String tweetUrl = "https://twitter.com/intent/tweet?text=" + Uri.encode(twitterMessage);
		final Uri uri = Uri.parse(tweetUrl);
		activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

	public void openRegister(Activity activity, String regURL) {
		final Uri uri = Uri.parse(regURL);
		activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

	public void reportIssue(Activity activity) {
		//final Uri uri = Uri.parse("https://github.com/devoxx/AndroidMobileApp/issues");
		final Uri uri = Uri.parse("mailto:info@exteso.com");
		activity.startActivity(new Intent(Intent.ACTION_VIEW, uri));
	}

	private static final String PLACEHOLDER = "Feedback: %1$s %2$s (%3$s) Android %4$s (API %5$s) %6$s %7$s\n\n";

	private String buildBasicInfo(Activity a) {
		return String.format(PLACEHOLDER,
				a.getString(R.string.app_name),
				getVersionName(a),
				getVersionCode(a),
				Build.VERSION.RELEASE,
				Build.VERSION.SDK_INT,
				Build.MANUFACTURER,
				Build.MODEL);
	}

	private static String getVersionName(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return "versionName";
		}
	}

	private static int getVersionCode(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
