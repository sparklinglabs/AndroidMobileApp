package com.devoxx.data.downloader;

import com.devoxx.connection.Connection;
import com.devoxx.connection.cfp.model.ConferenceApiModel;
import com.devoxx.data.cache.ConferencesCache;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@EBean
public class ConferenceDownloader {

	@Bean
	Connection connection;

	@Bean
	ConferencesCache conferencesCache;

	public void initWitStaticData() {
		conferencesCache.initWithFallbackData();
	}

	public List<ConferenceApiModel> fetchAllConferences() throws IOException {
		final List<ConferenceApiModel> result = conferencesCache.getData();
		if (result == null || !conferencesCache.isValid()) {
			final Call<List<ConferenceApiModel>> call = connection.getCfpApi().conferences();
			call.enqueue(new Callback<List<ConferenceApiModel>>() {
				@Override
				public void onResponse(Call<List<ConferenceApiModel>> call, Response<List<ConferenceApiModel>> response) {
					conferencesCache.upsert(response.body());
				}

				@Override public void onFailure(Call<List<ConferenceApiModel>> call, Throwable t) {
					conferencesCache.initWithFallbackData();
				}
			});
		}
		return result;
	}
}
