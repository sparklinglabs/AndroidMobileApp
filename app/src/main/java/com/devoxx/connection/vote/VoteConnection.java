package com.devoxx.connection.vote;

import com.annimon.stream.Optional;
import com.devoxx.BuildConfig;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.model.RealmConference;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@EBean(scope = EBean.Scope.Singleton)
public class VoteConnection {

	@Bean
	ConferenceManager conferenceManager;

	private VoteApi voteApi;

	public void setupApi(String apiUrl) {
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		if (BuildConfig.DEBUG) {
			final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addInterceptor(httpLoggingInterceptor);
		}

		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(apiUrl)
				.client(builder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		voteApi = retrofit.create(VoteApi.class);
	}

	public VoteApi getVoteApi() {
		if (voteApi == null) {
			final Optional<RealmConference> conference = conferenceManager.getActiveConference();
			if (conference.isPresent()) {
				setupApi(conference.get().getVotingURL());
			}
		}
		return voteApi;
	}
}
