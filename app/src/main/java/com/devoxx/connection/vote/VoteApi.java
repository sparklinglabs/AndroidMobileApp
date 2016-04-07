package com.devoxx.connection.vote;

import com.devoxx.connection.vote.model.VoteApiModel;
import com.devoxx.connection.vote.model.VoteApiSimpleModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Vote API based on that service:
 * <url>https://bitbucket.org/jonmort/devoxx-vote-api</url>
 */
public interface VoteApi {

	@POST("/{confCode}/vote") Call<VoteApiSimpleModel> vote(
			@Path("confCode") String confCode,
			@Body VoteApiModel model
	);

	@POST("/{confCode}/vote") Call<VoteApiSimpleModel> vote(
			@Path("confCode") String confCode,
			@Body VoteApiSimpleModel model
	);
}
