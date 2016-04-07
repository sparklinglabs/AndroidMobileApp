package com.devoxx.connection.vote.model;

import java.io.Serializable;

public class VoteApiSimpleModel implements Serializable {
	public final String talkId;
	public final String user;
	public final int rating;

	public VoteApiSimpleModel(String talkId, int rating, String user) {
		this.talkId = talkId;
		this.rating = rating;
		this.user = user;
	}
}
