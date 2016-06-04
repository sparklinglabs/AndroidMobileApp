package com.devoxx.android.activity;

import com.devoxx.R;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.user.UserManager;
import com.devoxx.integrations.IntegrationProvider;
import com.devoxx.utils.InfoUtil;
import com.google.android.gms.vision.barcode.Barcode;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import io.scalac.scanner.BarcodeCaptureActivity;

@EActivity(R.layout.activity_register_user)
public class RegisterUserActivity extends BaseActivity {

	private static final int RC_BARCODE_CAPTURE = 1578;

	@Bean
	InfoUtil infoUtil;

	@Bean
	IntegrationProvider integrationProvider;

	@Bean
	ConferenceManager conferenceManager;

	@Bean
	UserManager userManager;

	@ViewById(R.id.registerUserInfo)
	TextView userInfo;

	@ViewById(R.id.registerUserinput)
	EditText codeInput;

	private InfoExtractor infoExtractor = new EmptyExtractor();

	@Click(R.id.registerUserViaQr) void onScannerClick() {
		final Intent intent = new Intent(this, BarcodeCaptureActivity.class);
		intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
		startActivityForResult(intent, RC_BARCODE_CAPTURE);
	}

	@Click(R.id.registerUserSaveCode) void onSaveClick() {
		final String message;
		final boolean finishScreen;

		final String userId = infoExtractor.getUserId().second;
		final String input = codeInput.getText().toString();
		final String finalCode = validateInput(userId) ?
				userId : validateInput(input) ? input : null;

		if (validateInput(finalCode)) {
			userManager.saveUserCode(finalCode);
			message = getString(R.string.register_success_message);
			finishScreen = true;
			integrationProvider.provideIntegrationController()
					.userRegistered(conferenceManager.getActiveConference()
							.get().getIntegrationId(), finalCode, infoExtractor);
		} else {
			message = getString(R.string.register_failed_message);
			finishScreen = false;
		}

		infoUtil.showToast(message);

		if (finishScreen) {
			finish();
		}
	}

	private boolean validateInput(String input) {
		return !TextUtils.isEmpty(input);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (data != null && data.hasExtra(BarcodeCaptureActivity.BarcodeObject)) {
			final Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
			infoExtractor = new InfoExtractor(barcode.displayValue);
			fillUserInfo();
		}
	}

	private void fillUserInfo() {
		final String userId = infoExtractor.getUserId().second;
		final String userCompany = infoExtractor.getUserCompany().second;
		final String userName = infoExtractor.getUserName().second;
		final String userSurname = infoExtractor.getUserSurname().second;

		userInfo.setVisibility(View.VISIBLE);
		userInfo.setText(String.format("%s\n%s\n%s\n%s", userName, userSurname, userCompany, userId));

		codeInput.setVisibility(View.GONE);
	}

	public static class InfoExtractor {
		private String[] dataParts;

		public InfoExtractor(String data) {
			dataParts = data.split(",");
		}

		public Pair<String, String> getUserName() {
			return new Pair<>("userName", extractIfExists(dataParts, 1));
		}

		public Pair<String, String> getUserSurname() {
			return new Pair<>("userSurname", extractIfExists(dataParts, 2));
		}

		public Pair<String, String> getUserCompany() {
			return new Pair<>("userCompany", extractIfExists(dataParts, 3));
		}

		public Pair<String, String> getUserJob() {
			return new Pair<>("userJob", extractIfExists(dataParts, 5));
		}

		public Pair<String, String> getUserId() {
			return new Pair<>("userId", extractIfExists(dataParts, 0));
		}

		String extractIfExists(String[] array, int index) {
			return index >= array.length ? "" : array[index];
		}
	}

	private static class EmptyExtractor extends InfoExtractor {
		public EmptyExtractor() {
			super("");
		}

		@Override public Pair<String, String> getUserName() {
			return new Pair<>("userName", "");
		}

		@Override public Pair<String, String> getUserSurname() {
			return new Pair<>("userSurname", "");
		}

		@Override public Pair<String, String> getUserCompany() {
			return new Pair<>("userCompany", "");
		}

		@Override public Pair<String, String> getUserId() {
			return new Pair<>("userId", "");
		}

		public Pair<String, String> getUserJob() {
			return new Pair<>("userJob", "");
		}
	}
}
