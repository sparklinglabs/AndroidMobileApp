package com.devoxx.data.vote.voters;

import com.afollestad.materialdialogs.MaterialDialog;
import com.annimon.stream.Optional;
import com.devoxx.R;
import com.devoxx.connection.model.SlotApiModel;
import com.devoxx.connection.vote.VoteApi;
import com.devoxx.connection.vote.VoteConnection;
import com.devoxx.connection.vote.model.VoteApiModel;
import com.devoxx.connection.vote.model.VoteDetailsApiModel;
import com.devoxx.data.RealmProvider;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.model.RealmConference;
import com.devoxx.data.user.UserManager;
import com.devoxx.data.vote.VotedTalkModel;
import com.devoxx.data.vote.interfaces.IOnVoteForTalkListener;
import com.devoxx.data.vote.interfaces.ITalkVoter;
import com.devoxx.utils.Logger;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import retrofit.Call;
import retrofit.Response;

@EBean
public class TalkVoter implements ITalkVoter {

	private static final int NOT_READY_VOTE_HTTP_CODE = 500;
	private static final int ALREADY_VOTED_HTTP_CODE = 409;
	@Bean
	VoteConnection voteConnection;

	@Bean
	RealmProvider realmProvider;

	@Bean
	ConferenceManager conferenceManager;

	@Bean
	UserManager userManager;

	@Override
	public void showVoteDialog(Context context, SlotApiModel slot, IOnVoteForTalkListener listener) {
		final MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
		final MaterialDialog dialog = builder
				.customView(R.layout.talk_rating_layout, true)
				.positiveText(R.string.vote)
				.callback(new MaterialDialog.ButtonCallback() {
					@Override
					public void onPositive(MaterialDialog dialog) {
						super.onPositive(dialog);
						final View customView = dialog.getCustomView();
						final RatingBar ratingBar = (RatingBar) customView.findViewById(R.id.talkRatingBar);
						final int rating = (int) ratingBar.getRating();

						final String content = extractText(customView, R.id.talkRatingContentFeedback);
						final String delivery = extractText(customView, R.id.talkRatingDeliveryRemarks);
						final String other = extractText(customView, R.id.talkRatingOther);

						voteForTalk(rating, slot.talk.id, listener, content, delivery, other);
					}

					@NonNull private String extractText(View customView, int inputId) {
						return ((EditText) customView.
								findViewById(inputId)).getText().toString();
					}
				})
				.build();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		dialog.show();

		final View customView = dialog.getCustomView();
		final RatingBar ratingBar = (RatingBar) customView.findViewById(R.id.talkRatingBar);
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			DrawableCompat.setTint(ratingBar.getProgressDrawable(), Color.parseColor("#E3B505"));
		}

		final TextView title = (TextView) customView.findViewById(R.id.talkRatingTitle);
		title.setText(slot.talk.title);

		final TextView speakers = (TextView) customView.findViewById(R.id.talkRatingSpeakers);
		speakers.setText(slot.talk.getReadableSpeakers());
	}

	@Override
	public boolean isVotingEnabled() {
		final Optional<RealmConference> conferenceOptional = conferenceManager.getActiveConference();
		return conferenceOptional.isPresent() && Boolean.parseBoolean(conferenceOptional.get().getVotingEnabled());
	}

	@Override
	public boolean canVoteOnTalk(String talkId) {
		final Realm realm = realmProvider.getRealm();
		final VotedTalkModel model = realm.where(VotedTalkModel.class)
				.equalTo("talkId", talkId).findFirst();
		realm.close();
		return model == null;
	}

	private String getConfCode() {
		return conferenceManager.getActiveConferenceId().get();
	}

	@Background
	protected void voteForTalk(int rating, String talkId, IOnVoteForTalkListener listener, String content, String delivery, String other) {
		final Realm realm = realmProvider.getRealm();
		try {
			final VoteApi voteApi = voteConnection.getVoteApi();
			final Call<VoteApiModel> call = voteApi.vote(getConfCode(),
					prepareVoteModel(talkId, rating, content, delivery, other));
			final Response<VoteApiModel> response = call.execute();

			if (response.isSuccess()) {
				rememberVote(realm, talkId);
				notifyAboutSuccess(listener);
			} else if (response.code() == NOT_READY_VOTE_HTTP_CODE) {
				notifyAboutCantVote(listener);
			} else if (response.code() == ALREADY_VOTED_HTTP_CODE) {
				rememberVote(realm, talkId);
				notifyAboutCantVoteMore(listener);
			} else {
				notifyAboutError(listener, new IOException("Error: " + response.code()));
			}
		} catch (IOException e) {
			Logger.exc(e);
			notifyAboutError(listener, e);
		} finally {
			realm.close();
		}
	}

	private void rememberVote(Realm realm, String talkId) {
		realm.beginTransaction();
		realm.copyToRealm(new VotedTalkModel(talkId));
		realm.commitTransaction();
	}

	@UiThread void notifyAboutSuccess(IOnVoteForTalkListener listener) {
		if (listener != null) {
			listener.onVoteForTalkSucceed();
		}
	}

	@UiThread void notifyAboutError(IOnVoteForTalkListener listener, Exception e) {
		if (listener != null) {
			listener.onVoteForTalkFailed(e);
		}
	}

	@UiThread void notifyAboutCantVote(IOnVoteForTalkListener listener) {
		if (listener != null) {
			listener.onCantVoteOnTalkYet();
		}
	}

	@UiThread void notifyAboutCantVoteMore(IOnVoteForTalkListener listener) {
		if (listener != null) {
			listener.onCantVoteMoreThanOnce();
		}
	}

	private VoteApiModel prepareVoteModel(String talkId, int rating, String content, String delivery, String other) {
		final String userId = userManager.getUserCode();
		final List<VoteDetailsApiModel> details = new ArrayList<>();
		appendDetails(details, "content", content, rating);
		appendDetails(details, "delivery", delivery, rating);
		appendDetails(details, "other", other, rating);
		return new VoteApiModel(talkId, rating, userId, details);
	}

	private void appendDetails(List<VoteDetailsApiModel> result, String key, String value, int rating) {
		if (!TextUtils.isEmpty(value)) {
			result.add(new VoteDetailsApiModel(key, rating, value));
		}
	}
}
