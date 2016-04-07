package com.devoxx.connection;

import com.annimon.stream.Optional;
import com.devoxx.BuildConfig;
import com.devoxx.Configuration;
import com.devoxx.connection.cfp.CfpApi;
import com.devoxx.data.conference.ConferenceManager;
import com.devoxx.data.model.RealmConference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@EBean(scope = EBean.Scope.Singleton)
public class Connection {

	@RootContext
	Context context;

	@SystemService
	ConnectivityManager cm;

	@Pref
	ConnectionConfigurationStore_ connectionConfigurationStore;

	@Bean
	ConferenceManager conferenceManager;

	private DevoxxApi devoxxApi;
	private CfpApi cfpApi;

	@AfterInject void afterInject() {
		initiCfpApi();
	}

	public void setupConferenceApi(String conferenceEndpoint) {
		connectionConfigurationStore.edit().activeConferenceApiUrl()
				.put(conferenceEndpoint).apply();

		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		if (BuildConfig.DEBUG) {
			final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addInterceptor(httpLoggingInterceptor);
		}

		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(conferenceEndpoint)
				.client(builder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		devoxxApi = retrofit.create(DevoxxApi.class);
	}

	public DevoxxApi getDevoxxApi() {
		if (devoxxApi == null) {
			final Optional<RealmConference> conference = conferenceManager.getActiveConference();
			if (conference.isPresent()) {
				setupConferenceApi(conference.get().getCfpURL());
			}
		}
		return devoxxApi;
	}

	public CfpApi getCfpApi() {
		return cfpApi;
	}

	public String getActiveConferenceApiUrl() {
		return connectionConfigurationStore.activeConferenceApiUrl().get();
	}

	private void initiCfpApi() {
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		if (BuildConfig.DEBUG) {
			final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addInterceptor(httpLoggingInterceptor);
		}

		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(Configuration.CFP_API_URL)
				.client(builder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		cfpApi = retrofit.create(CfpApi.class);
	}

	public boolean isOnline() {
		final NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}
}
