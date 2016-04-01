package com.devoxx.data.vote.interfaces;

import com.devoxx.connection.model.SlotApiModel;

import android.content.Context;

public interface ITalkVoter {

	boolean isVotingEnabled();

	boolean isAlreadyVoted(String talkId);

	void showVoteDialog(Context context, SlotApiModel slot, IOnVoteForTalkListener listener);

	boolean canVoteForTalk(SlotApiModel slotModel);
}
