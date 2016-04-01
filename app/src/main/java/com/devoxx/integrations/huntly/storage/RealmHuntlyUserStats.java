package com.devoxx.integrations.huntly.storage;

import com.devoxx.integrations.huntly.connection.model.HuntlyUserStats;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmHuntlyUserStats extends RealmObject {
	@PrimaryKey private long userId;
	private int points;
	private String username;

	public static RealmHuntlyUserStats fromApi(HuntlyUserStats stats) {
		final RealmHuntlyUserStats result = new RealmHuntlyUserStats();
		result.setPoints(stats.getPoints());
		result.setUserId(stats.getUserId());
		result.setUsername(stats.getUsername());
		return result;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
