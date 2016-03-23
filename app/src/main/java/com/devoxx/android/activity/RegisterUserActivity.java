package com.devoxx.android.activity;

import com.devoxx.R;
import com.devoxx.data.user.UserManager;
import com.devoxx.utils.InfoUtil;
import com.google.android.gms.vision.barcode.Barcode;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import android.content.Intent;
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
	UserManager userManager;

	@ViewById(R.id.registerUserInfo)
	TextView userInfo;

	@ViewById(R.id.registerUserinput)
	EditText codeInput;

	private String userId;

	@Click(R.id.registerUserViaQr) void onScannerClick() {
		final Intent intent = new Intent(this, BarcodeCaptureActivity.class);
		intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
		startActivityForResult(intent, RC_BARCODE_CAPTURE);
	}

	@Click(R.id.registerUserSaveCode) void onSaveClick() {
		final String message;
		final boolean finishScreen;

		final String input = codeInput.getText().toString();
		final String finalCode = validateInput(userId) ?
				userId : validateInput(input) ? input : null;

		if (validateInput(finalCode)) {
			userManager.saveUserCode(finalCode);
			message = getString(R.string.register_success_message);
			finishScreen = true;
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
			final String code = barcode.displayValue;
			extractInfo(code);
		}
	}

	private void extractInfo(String data) {
		final String[] dataParts = data.split(",");
		userId = dataParts[0];
		final String userCompany = dataParts[1];
		final String userName = dataParts[2];
		final String userSurname = dataParts[3];

		userInfo.setVisibility(View.VISIBLE);
		userInfo.setText(String.format("%s\n%s\n%s\n%s", userName, userSurname, userCompany, userId));

		codeInput.setVisibility(View.GONE);
	}
}
