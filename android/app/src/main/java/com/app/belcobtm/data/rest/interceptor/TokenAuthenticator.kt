package com.app.belcobtm.data.rest.interceptor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.HostActivity.Companion.FORCE_UNLINK_KEY
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection


class TokenAuthenticator(
    private val context: Context,
    private val authApi: AuthApi,
    private val prefsHelper: SharedPreferencesHelper,
    private val settingsApi: SettingsApi,
    private val connectionHandler: WalletConnectionHandler,
    private val daoAccount: AccountDao
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = prefsHelper.refreshToken
        val refereshBody = RefreshTokenRequest(refreshToken)
        val authResponse = authApi.refereshToken(refereshBody).execute()
        val responseBody = authResponse.body()
        val responseCode = authResponse.code()
        return when {
            responseCode == HttpURLConnection.HTTP_OK && responseBody != null -> {
                val updatedToken = responseBody.accessToken
                // update session
                prefsHelper.processAuthResponse(responseBody)
                // return old request with updated token
                return response.request().newBuilder()
                    .header(
                        BaseInterceptor.HEADER_AUTHORIZATION_KEY,
                        prefsHelper.formatToken(updatedToken)
                    )
                    .build()
            }
            response.code() == HttpURLConnection.HTTP_FORBIDDEN ||
                    response.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                runBlocking {
                    settingsApi.unlink(prefsHelper.userId.toString()).await()
                    daoAccount.clearTable()
                    prefsHelper.clearData()
                    connectionHandler.disconnect()
                }
                context.startActivity(Intent(context, HostActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtras(Bundle().apply {
                        putBoolean(FORCE_UNLINK_KEY, true)
                    })
                })
                null
            }
            else -> null
        }
    }
}
