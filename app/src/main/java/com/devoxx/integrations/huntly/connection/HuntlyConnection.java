package com.devoxx.integrations.huntly.connection;

import com.devoxx.BuildConfig;
//import com.devoxx.huntlyintegration.Encryption;
import com.devoxx.integrations.huntly.HuntlyController;
import com.devoxx.integrations.huntly.connection.model.HuntlyActivityCompleteResponse;
import com.devoxx.integrations.huntly.connection.model.HuntlyDeepLinkConf;
import com.devoxx.integrations.huntly.connection.model.HuntlyEvent;
import com.devoxx.integrations.huntly.connection.model.HuntlyProfileProperty;
import com.devoxx.integrations.huntly.connection.model.HuntlyPromo;
import com.devoxx.integrations.huntly.connection.model.HuntlyQuestActivity;
import com.devoxx.integrations.huntly.connection.model.HuntlyRegisterResponse;
import com.devoxx.integrations.huntly.connection.model.HuntlyUserStats;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import android.util.Base64;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@EBean(scope = EBean.Scope.Singleton)
public class HuntlyConnection {

	@Bean HuntlyController huntlyController;

	private HuntlyApi huntlyApi;

	public void init() {
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();

		if (BuildConfig.DEBUG) {
			final HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
			httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
			builder.addInterceptor(httpLoggingInterceptor);
		}

		builder.addInterceptor(new AuthInterceptor());

		builder.sslSocketFactory(setupSsl());
		builder.hostnameVerifier((hostname, session) -> true);

		final Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://srv.huntlyapp.com/")
				.client(builder.build())
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		huntlyApi = retrofit.create(HuntlyApi.class);
	}

	public Response<HuntlyRegisterResponse> login(String id) throws IOException {
		return huntlyApi.login(id, "android").execute();
	}

	public Response<List<HuntlyEvent>> events() throws IOException {
		return huntlyApi.events().execute();
	}

	public Response<List<HuntlyQuestActivity>> activities(long id) throws IOException {
		return huntlyApi.activityQuest(id).execute();
	}

	public Response<HuntlyActivityCompleteResponse> completeQuest(long questId) throws IOException {
		return huntlyApi.activityComplete(questId).execute();
	}

	private static SSLSocketFactory setupSsl() {
		try {
			final TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
						}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
						}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}
			};

			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			return sslContext.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Response<HuntlyDeepLinkConf> deepLinks(long id) throws IOException {
		return huntlyApi.deepLinks(id).execute();
	}

	public Response<HuntlyUserStats> userStats(long id) throws IOException {
		return huntlyApi.userStats(id).execute();
	}

	public Response<Void> updateUserProfile(long id, List<HuntlyProfileProperty> properties) throws IOException {
		return huntlyApi.profileFill(id, properties).execute();
	}

	public Response<HuntlyPromo> promo(long id) throws IOException {
		return huntlyApi.promo(id).execute();
	}

	class AuthInterceptor implements Interceptor {
		@Override public okhttp3.Response intercept(Chain chain) throws IOException {
			//final byte[] bytes = new Encryption().getCredentials().getBytes();
			final byte[] bytes = new byte[64];
			final String basic = "Basic " + Base64.encodeToString(bytes, Base64.NO_WRAP);
			final Request original = chain.request();
			final Request.Builder requestBuilder = original.newBuilder()
					.header("Authorization", basic)
					.header("X-AUTH-TOKEN", huntlyController.token())
					.method(original.method(), original.body());


			return chain.proceed(requestBuilder.build());
		}
	}
}
