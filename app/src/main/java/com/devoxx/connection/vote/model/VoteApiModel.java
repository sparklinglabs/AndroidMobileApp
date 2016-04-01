package com.devoxx.connection.vote.model;

import java.io.Serializable;
import java.util.List;

public class VoteApiModel implements Serializable {
	public final String talkId;
	public final String user;
	public final List<VoteDetailsApiModel> details;

	public VoteApiModel(String talkId, String user, List<VoteDetailsApiModel> details) {
		this.talkId = talkId;
		this.user = user;
		this.details = details;
	}
}
