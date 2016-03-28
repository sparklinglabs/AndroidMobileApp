package com.devoxx.connection.vote.model;

import java.io.Serializable;
import java.util.List;

public class VoteApiModel implements Serializable {
	public final String talkId;
	public final String user;
	public final int rating;
	public final List<VoteDetailsApiModel> details;

	public VoteApiModel(String talkId, int rating, String user, List<VoteDetailsApiModel> details) {
		this.talkId = talkId;
		this.rating = rating;
		this.user = user;
		this.details = details;
	}
}
