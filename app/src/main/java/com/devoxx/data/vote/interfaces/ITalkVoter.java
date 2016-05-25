package com.devoxx.data.vote.interfaces;

import com.devoxx.connection.model.SlotApiModel;

import android.app.Activity;

public interface ITalkVoter {

	boolean isVotingEnabled();

	boolean isAlreadyVoted(String talkId);

	void showVoteDialog(Activity context, SlotApiModel slot, IOnVoteForTalkListener listener);

	boolean canVoteForTalk(SlotApiModel slotModel);
}
