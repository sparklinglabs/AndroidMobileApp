package com.devoxx.integrations.huntly.storage;

import com.devoxx.integrations.huntly.connection.model.HuntlyQuestActivity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmHuntlyQuestActivity extends RealmObject {
	@PrimaryKey private long questId;
	@HuntlyQuestActivity.QuestActivity private String activity;
	private int singleReward;
	private int performedActions;
	private int maxActions;

	public static RealmHuntlyQuestActivity fromApi(HuntlyQuestActivity ac) {
		final RealmHuntlyQuestActivity result = new RealmHuntlyQuestActivity();
		result.setActivity(ac.getActivity());
		result.setQuestId(ac.getQuestId());
		result.setSingleReward(ac.getSingleReward());
		result.setPerformedActions(ac.getPerformedActivities());
		result.setMaxActions(ac.getMaxActivities());
		return result;
	}

	@HuntlyQuestActivity.QuestActivity public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public long getQuestId() {
		return questId;
	}

	public void setQuestId(long questId) {
		this.questId = questId;
	}

	public int getSingleReward() {
		return singleReward;
	}

	public void setSingleReward(int singleReward) {
		this.singleReward = singleReward;
	}

	public int getPerformedActions() {
		return performedActions;
	}

	public void setPerformedActions(int performedActions) {
		this.performedActions = performedActions;
	}

	public int getMaxActions() {
		return maxActions;
	}

	public void setMaxActions(int maxActions) {
		this.maxActions = maxActions;
	}
}
