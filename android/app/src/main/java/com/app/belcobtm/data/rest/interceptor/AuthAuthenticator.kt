package com.app.belcobtm.data.rest.interceptor

import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection

class AuthAuthenticator(
    private val prefHelper: SharedPreferencesHelper,
    private val authApi: AuthApi
//    , private val broadcastManager: LocalBroadcastManager
) : Authenticator {
    private var authorizeFail: Byte = 0

    override fun authenticate(route: Route?, response: Response): Request? {
        val request: Request = response.request()
        val refreshToken: String = prefHelper.refreshToken
        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            checkAuthorizeTimes()
        }

        return if (refreshToken.isNotBlank()) {
            val refreshTokenRequest = runBlocking {
                authApi.signInByRefreshTokenAsync(RefreshTokenRequest(refreshToken)).await()
            }
            val accessToken = refreshTokenRequest.body()?.accessToken
            if (!accessToken.isNullOrBlank()) {
                prefHelper.accessToken = accessToken
                authorizeFail = 0

                request.newBuilder()
                    .header(ApiFactory.HEADER_AUTHORIZATION_KEY, prefHelper.accessToken)
                    .build()
            } else {
//                broadcastManager.sendBroadcast(Intent(TAG_USER_UNAUTHORIZED))
                request
            }
        } else {
//            broadcastManager.sendBroadcast(Intent(TAG_USER_UNAUTHORIZED))
            request
        }
    }

    private fun checkAuthorizeTimes() {
        if (authorizeFail > MAX_NOT_AUTHORIZE_TIMES) {
//            prefHelper.accessToken = ""
//            prefHelper.refreshToken = ""
//            prefHelper.userId = -1
            authorizeFail = 0
        } else {
            authorizeFail++
        }
    }

    companion object {
        private const val MAX_NOT_AUTHORIZE_TIMES: Byte = 3
//        const val TAG_USER_UNAUTHORIZED = "tag_broadcast_user_unauthorized"
    }
}