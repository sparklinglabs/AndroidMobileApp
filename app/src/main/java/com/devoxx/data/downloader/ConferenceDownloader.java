package com.devoxx.data.downloader;

import com.crashlytics.android.Crashlytics;
import com.devoxx.connection.Connection;
import com.devoxx.connection.cfp.model.ConferenceApiModel;
import com.devoxx.data.cache.ConferencesCache;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
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
		/*try {
			final Call<List<ConferenceApiModel>> call = connection.getCfpApi().conferences();
			final Response<List<ConferenceApiModel>> response = call.execute();
			if (response.isSuccessful()) {
				conferencesCache.upsert(response.body());
			} else {
				conferencesCache.initWithFallbackData();
			}
		} catch (Exception e) {
			conferencesCache.initWithFallbackData();
			Crashlytics.logException(e);
		}*/

		conferencesCache.initWithFallbackData();
		return conferencesCache.getData();
	}
}
