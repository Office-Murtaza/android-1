package com.belcobtm.data.rest.interceptor

import android.util.Log
import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.authorization.AuthApi
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.data.websockets.manager.WebSocketManager
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection

class TokenAuthenticator(
    private val authApi: AuthApi,
    private val walletDao: WalletDao,
    private val prefsHelper: SharedPreferencesHelper,
    private val unlinkHandler: UnlinkHandler,
    private val webSocketManager: WebSocketManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = prefsHelper.refreshToken
        val refereshBody = RefreshTokenRequest(refreshToken)
        Log.d("REFRESH", "From TokenAuthenticator")
        val authResponse = authApi.refreshToken(refereshBody).execute()
        val responseBody = authResponse.body()
        val responseCode = authResponse.code()
        return when {
            responseCode == HttpURLConnection.HTTP_OK && responseBody != null -> {
                val updatedToken = responseBody.accessToken
                runBlocking {
                    walletDao.updateBalance(responseBody.balance)
                }
                prefsHelper.processAuthResponse(responseBody) // update session
                runBlocking {
                    webSocketManager.connect()
                }
                return response.request.newBuilder()  // return old request with updated token
                    .header(
                        BaseInterceptor.HEADER_AUTHORIZATION_KEY,
                        prefsHelper.formatToken(updatedToken)
                    )
                    .build()
            }
            response.code == HttpURLConnection.HTTP_FORBIDDEN ||
                response.code == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                runBlocking {
                    webSocketManager.disconnect()
                    unlinkHandler.performUnlink()
                }
                null
            }
            else -> null
        }
    }

}
