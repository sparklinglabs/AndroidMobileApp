package com.devoxx.integrations.huntly.connection.model;

import com.devoxx.integrations.huntly.storage.RealmHuntlyUserStats;

import java.io.Serializable;

public class HuntlyUserStats implements Serializable {
	private long userId;
	private int points;
	private String username;

	public static HuntlyUserStats fromDb(RealmHuntlyUserStats stats) {
		if (stats == null) {
			return null;
		}

		final HuntlyUserStats result = new HuntlyUserStats();
		result.userId = stats.getUserId();
		result.points = stats.getPoints();
		result.username = stats.getUsername();
		return result;
	}

	public long getUserId() {
		return userId;
	}

	public int getPoints() {
		return points;
	}

	public String getUsername() {
		return username;
	}

	@Override public String toString() {
		return "HuntlyUserStats{" +
				"userId=" + userId +
				", points=" + points +
				", username='" + username + '\'' +
				'}';
	}
}
