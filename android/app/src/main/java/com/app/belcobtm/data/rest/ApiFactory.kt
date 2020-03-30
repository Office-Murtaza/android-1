package com.app.belcobtm.data.rest

import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(private val prefHelper: SharedPreferencesHelper) {
    private val loggingInterceptor: HttpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val baseInterceptor: Interceptor = Interceptor {
        val request = it.request()
            .newBuilder()
            .addHeader(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE)
            .addHeader(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE)
            .addHeader(HEADER_ACCEPT_KEY, HEADER_ACCEPT_VALUE)
            .addHeader(HEADER_AUTHORIZATION_KEY, getAccessToken())
            .build()
        it.proceed(request)
    }

    private val errorInterceptor = ResponseInterceptor()

    private val baseHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .readTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(baseInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(errorInterceptor)
        .build()

    val authApi: AuthApi = retrofit(baseHttpClient).create(AuthApi::class.java)
    val settingsApi: SettingsApi = retrofit(baseHttpClient).create(SettingsApi::class.java)

    private fun retrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(SERVER_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    private fun getAccessToken(): String =
        if (prefHelper.accessToken.isNullOrBlank()) "" else HEADER_AUTHORIZATION_VALUE + prefHelper.accessToken

    companion object {
        // private const val BASE_URL = "https://prod.belcobtm.com"
        // private const val BASE_URL = "http://206.189.204.44:8080"
        private const val BASE_URL = "https://test.belcobtm.com"
        private const val API_VERSION = 1
         const val SERVER_URL = "$BASE_URL/api/v$API_VERSION/"

        private const val HEADER_CONTENT_TYPE_KEY = "Content-Type"
        private const val HEADER_CONTENT_TYPE_VALUE = "application/json"
        private const val HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With"
        private const val HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest"
        private const val HEADER_ACCEPT_KEY = "Accept"
        private const val HEADER_ACCEPT_VALUE = "application/json"
        private const val WAIT_TIME_SECONDS = 60L
        private const val HEADER_AUTHORIZATION_KEY = "Authorization"
        private const val HEADER_AUTHORIZATION_VALUE = "Bearer "
    }
}