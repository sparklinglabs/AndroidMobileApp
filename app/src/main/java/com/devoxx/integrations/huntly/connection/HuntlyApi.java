package com.devoxx.integrations.huntly.connection;

import com.devoxx.integrations.huntly.connection.model.HuntlyActivityCompleteResponse;
import com.devoxx.integrations.huntly.connection.model.HuntlyActivityQuestPromo;
import com.devoxx.integrations.huntly.connection.model.HuntlyDeepLinkConf;
import com.devoxx.integrations.huntly.connection.model.HuntlyEvent;
import com.devoxx.integrations.huntly.connection.model.HuntlyProfileProperty;
import com.devoxx.integrations.huntly.connection.model.HuntlyPromo;
import com.devoxx.integrations.huntly.connection.model.HuntlyQuestActivity;
import com.devoxx.integrations.huntly.connection.model.HuntlyRegisterResponse;
import com.devoxx.integrations.huntly.connection.model.HuntlyUserStats;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface HuntlyApi {

	@FormUrlEncoded
	@POST("/users/login/platform")
	Call<HuntlyRegisterResponse> login(@Field("uid") String uid, @Field("platform") String platform);

	@GET("/deployments/{id}/quests/activity/list")
	Call<List<HuntlyQuestActivity>> activityQuest(@Path("id") long eventId);

	@FormUrlEncoded
	@POST("/quests/activity/complete")
	Call<HuntlyActivityCompleteResponse> activityComplete(@Field("questId") long questid);

	@GET("/deployments/{id}/quests/activity/promo")
	Call<HuntlyActivityQuestPromo> activityQuestPromo(@Path("id") String eventId);

	@GET("/deployments/{id}/user") Call<HuntlyUserStats> userStats(@Path("id") long eventId);

	@GET("/deployments") Call<List<HuntlyEvent>> events();

	@GET("/deployments/{id}/deeplink") Call<HuntlyDeepLinkConf> deepLinks(@Path("id") long eventId);

	@POST("/deployments/{id}/profile/fill")
	Call<Void> profileFill(@Path("id") long eventId, @Body List<HuntlyProfileProperty> properties);

	@GET("/deployments/{id}/quests/activity/promo") Call<HuntlyPromo> promo(@Path("id") long eventId);
}
