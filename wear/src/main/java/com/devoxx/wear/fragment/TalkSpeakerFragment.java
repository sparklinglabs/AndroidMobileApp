package com.devoxx.wear.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.ConfirmationActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.devoxx.R;
import com.devoxx.common.utils.Constants;
import com.devoxx.event.TwitterEvent;
import com.devoxx.event.DisplaySpeakerEvent;
import com.devoxx.event.GetSpeakerEvent;
import com.devoxx.event.SpeakerDetailEvent;
import com.devoxx.model.TalkSpeakerApiModel;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.tajchert.buswear.EventBus;

/**
 * Created by eloudsa on 24/08/15.
 */
public class TalkSpeakerFragment extends BaseFragment {

    private final static String TAG = TalkSpeakerFragment.class.getCanonicalName();

    private View mMainView;
    private String mSpeakerId;

    private TalkSpeakerApiModel mCurrentSpeaker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.talk_speaker_fragment, container, false);

        mSpeakerId = (getArguments() != null ? getArguments().getString(Constants.DATAMAP_UUID) : "");

        mMainView.findViewById(R.id.twitterIcon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConfirmationActivity(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION, getString(R.string.confirmation_open_on_phone));
                EventBus.getDefault().postLocal(new TwitterEvent(Constants.CHANNEL_ID + Constants.TWITTER_PATH, (String) mMainView.findViewById(R.id.twitterIcon).getTag()));
            }
        });


        return mMainView;
    }



    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().postLocal(new GetSpeakerEvent(mSpeakerId));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }


    // Events

    public void onEvent(DisplaySpeakerEvent displaySpeakerEvent) {

        if (displaySpeakerEvent == null) {
            return;
        }

        if (displaySpeakerEvent.getSpeaker() == null) {
            return;
        }

        // check if this event is related to this card
        if (displaySpeakerEvent.getSpeaker().getUuid().equalsIgnoreCase(mSpeakerId) == false) {
            return;
        }

        mCurrentSpeaker = displaySpeakerEvent.getSpeaker();

        displaySpeaker();

    }


    public void onEvent(SpeakerDetailEvent speakerDetailEvent) {

        if (speakerDetailEvent == null) {
            return;
        }

        if (speakerDetailEvent.getSpeaker() == null) {
            return;
        }

        // check if this event is related to this card
        if (speakerDetailEvent.getSpeaker().getUuid().equalsIgnoreCase(mSpeakerId) == false) {
            return;
        }

        mCurrentSpeaker = speakerDetailEvent.getSpeaker();

        displaySpeaker();

    }


    private void displaySpeaker() {

        if (mCurrentSpeaker == null) {
            return;
        }

        if (getActivity() == null) {
            return;
        }

        // set the detail on the layout
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                String fullName = mCurrentSpeaker.getName();
                if ((mCurrentSpeaker.getFirstName() != null) && (mCurrentSpeaker.getLastName() != null)) {
                    fullName = mCurrentSpeaker.getFirstName() + " " + mCurrentSpeaker.getLastName();
                }

                ((TextView) mMainView.findViewById(R.id.title)).setText(fullName);

                if ((mCurrentSpeaker.getAvatarImage() != null) && !(mCurrentSpeaker.getAvatarImage().isEmpty())) {
                    byte[] decodedString = Base64.decode(mCurrentSpeaker.getAvatarImage(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    Drawable drawable = (Drawable) new BitmapDrawable(getActivity().getResources(), bitmap);

                    ((CircleImageView) mMainView.findViewById(R.id.profile_image)).setImageDrawable(drawable);
                }


                if (TextUtils.isEmpty(mCurrentSpeaker.getTwitter()) == false) {
                    String twitterName = mCurrentSpeaker.getTwitter().toLowerCase();

                    mMainView.findViewById(R.id.twitterLayout).setVisibility(View.VISIBLE);
                    mMainView.findViewById(R.id.twitterIcon).setTag(twitterName);
                } else {
                    mMainView.findViewById(R.id.twitterLayout).setVisibility(View.GONE);
                    mMainView.findViewById(R.id.twitterIcon).setTag("");
                }
            }
        });
    }

}
