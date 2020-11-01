package com.app.belcobtm.data.rest.interceptor

import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection


class TokenAuthenticator(
    private val authApi: AuthApi,
    private val prefsHelper: SharedPreferencesHelper,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = prefsHelper.refreshToken
        val refereshBody = RefreshTokenRequest(refreshToken)
        val authResponse = authApi.refereshToken(refereshBody).execute()
        if (authResponse.code() == HttpURLConnection.HTTP_OK) {
            val authModel = authResponse.body()!!
            val updatedToken = authModel.accessToken
            // update session
            prefsHelper.processAuthResponse(authModel)
            // return old request with updated token
            return response.request().newBuilder()
                .header(BaseInterceptor.HEADER_AUTHORIZATION_KEY, updatedToken)
                .build()
        }
        return null
    }
}
