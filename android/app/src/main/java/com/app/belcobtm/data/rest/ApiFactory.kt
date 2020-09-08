package com.app.belcobtm.data.rest

import com.app.belcobtm.BuildConfig
import com.app.belcobtm.data.rest.atm.AtmApi
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.rest.tools.ToolsApi
import com.app.belcobtm.data.rest.transaction.TransactionApi
import com.app.belcobtm.data.rest.wallet.WalletApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ApiFactory(
    private val okHttpClient: OkHttpClient
) {

    val authApi: AuthApi = createApiWithSessionClient(AuthApi::class.java)
    val settingsApi: SettingsApi = createApiWithSessionClient(SettingsApi::class.java)
    val walletApi: WalletApi = createApiWithSessionClient(WalletApi::class.java)
    val transactionApi: TransactionApi = createApiWithSessionClient(TransactionApi::class.java)
    val toolsApi: ToolsApi = createApiWithSessionClient(ToolsApi::class.java)
    val atmApi: AtmApi = createApiWithSessionClient(AtmApi::class.java)

    private fun <T> createApiWithSessionClient(clazz: Class<T>): T = Retrofit.Builder()
        .baseUrl(SERVER_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(okHttpClient)
        .build()
        .create(clazz)

    companion object {
        const val SERVER_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/"
        const val SOCKET_URL = "${BuildConfig.BASE_URL}/api/v${BuildConfig.API_VERSION}/ws"
    }
}



