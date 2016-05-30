package com.devoxx.connection.vote;

import com.devoxx.connection.vote.model.VoteApiModel;
import com.devoxx.connection.vote.model.VoteApiSimpleModel;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Vote API based on that service:
 * <url>https://bitbucket.org/jonmort/devoxx-vote-api</url>
 */
public interface VoteApi {

	@POST("/api/voting/v1/vote") Call<VoteApiSimpleModel> vote(@Body VoteApiModel model);

	@POST("/api/voting/v1/vote") Call<VoteApiSimpleModel> vote(@Body VoteApiSimpleModel model);
}
