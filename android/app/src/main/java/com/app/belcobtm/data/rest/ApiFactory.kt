package com.app.belcobtm.data.rest

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.interceptor.AuthAuthenticator
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.rest.tools.ToolsApi
import com.app.belcobtm.data.rest.transaction.TransactionApi
import com.app.belcobtm.data.rest.wallet.WalletApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(private val prefHelper: SharedPreferencesHelper) {
    private val baseInterceptor: Interceptor = Interceptor {
        val request = it.request()
            .newBuilder()
            .addHeader(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE)
            .addHeader(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE)
            .addHeader(HEADER_ACCEPT_KEY, HEADER_ACCEPT_VALUE)
            .addHeader(HEADER_AUTHORIZATION_KEY, prefHelper.accessToken)
            .build()
        it.proceed(request)
    }

    private val sessionHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .readTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(baseInterceptor)
//        .addInterceptor(LogInterceptor())
        .addInterceptor(ResponseInterceptor())
        .authenticator(AuthAuthenticator(prefHelper, createApi(AuthApi::class.java)))
        .build()

    val authApi: AuthApi = createApiWithSessionClient(AuthApi::class.java)
    val settingsApi: SettingsApi = createApiWithSessionClient(SettingsApi::class.java)
    val walletApi: WalletApi = createApiWithSessionClient(WalletApi::class.java)
    val transactionApi: TransactionApi = createApiWithSessionClient(TransactionApi::class.java)
    val toolsApi: ToolsApi = createApiWithSessionClient(ToolsApi::class.java)

    private fun <T> createApi(clazz: Class<T>): T = Retrofit.Builder()
        .baseUrl(SERVER_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
        .create(clazz)

    private fun <T> createApiWithSessionClient(clazz: Class<T>): T = Retrofit.Builder()
        .baseUrl(SERVER_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(sessionHttpClient)
        .build()
        .create(clazz)

    companion object {
        // private const val BASE_URL = "https://prod.belcobtm.com"
        private const val BASE_URL = "http://test.belcobtm.com"

        // private const val BASE_URL = "http://161.35.22.9"
        private const val API_VERSION = 1
        const val SERVER_URL = "$BASE_URL/api/v$API_VERSION/"

        private const val HEADER_CONTENT_TYPE_KEY = "Content-Type"
        private const val HEADER_CONTENT_TYPE_VALUE = "application/json"
        private const val HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With"
        private const val HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest"
        private const val HEADER_ACCEPT_KEY = "Accept"
        private const val HEADER_ACCEPT_VALUE = "application/json"
        private const val WAIT_TIME_SECONDS = 60L
        const val HEADER_AUTHORIZATION_KEY = "Authorization"
    }
}



