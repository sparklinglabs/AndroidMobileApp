package com.devoxx.wear.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.support.wearable.activity.ConfirmationActivity;

/**
 * Created by eloudsa on 28/03/16.
 */
public abstract class BaseFragment extends Fragment {


    public void startConfirmationActivity(int animationType, String message) {
        Intent confirmationActivity = new Intent(getActivity(), ConfirmationActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, animationType)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, message);

        getActivity().startActivity(confirmationActivity);
    }

}
