package com.devoxx.connection.vote.model;

import java.io.Serializable;

public class VoteDetailsApiModel implements Serializable {
	public final String review;
	public final String aspect;
	public final int rating;

	public VoteDetailsApiModel(String aspect, int rating, String review) {
		this.review = review;
		this.rating = rating;
		this.aspect = aspect;
	}
}
