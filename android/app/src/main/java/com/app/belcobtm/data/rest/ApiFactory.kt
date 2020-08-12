package com.app.belcobtm.data.rest

import com.app.belcobtm.BuildConfig
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.LogInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.rest.tools.ToolsApi
import com.app.belcobtm.data.rest.transaction.TransactionApi
import com.app.belcobtm.data.rest.wallet.WalletApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ApiFactory(
    noConnectionInterceptor: NoConnectionInterceptor,
    baseInterceptor: BaseInterceptor,
    responseInterceptor: ResponseInterceptor,
    logInterceptor: LogInterceptor
) {
    private val sessionHttpClient = OkHttpClient().newBuilder()
        .connectTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .readTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(noConnectionInterceptor)
        .addInterceptor(baseInterceptor)
        .addInterceptor(logInterceptor)
        .addInterceptor(responseInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val authApi: AuthApi = createApiWithSessionClient(AuthApi::class.java)
    val settingsApi: SettingsApi = createApiWithSessionClient(SettingsApi::class.java)
    val walletApi: WalletApi = createApiWithSessionClient(WalletApi::class.java)
    val transactionApi: TransactionApi = createApiWithSessionClient(TransactionApi::class.java)
    val toolsApi: ToolsApi = createApiWithSessionClient(ToolsApi::class.java)

    private fun <T> createApiWithSessionClient(clazz: Class<T>): T = Retrofit.Builder()
        .baseUrl(SERVER_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(sessionHttpClient)
        .build()
        .create(clazz)

    companion object {
        const val SERVER_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/"
        private const val WAIT_TIME_SECONDS = 60L
    }
}



