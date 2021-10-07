package com.belcobtm.data.rest.interceptor

import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.rest.authorization.AuthApi
import com.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.belcobtm.domain.service.ServiceRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.net.HttpURLConnection


class TokenAuthenticator(
    private val authApi: AuthApi,
    private val serviceRepository: ServiceRepository,
    private val prefsHelper: SharedPreferencesHelper,
    private val unlinkHandler: UnlinkHandler
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
                runBlocking {
                    serviceRepository.updateServices(
                        responseBody.user.availableServices,
                        responseBody.serviceFees
                    )

                }
                // update session
                prefsHelper.processAuthResponse(responseBody)
                // return old request with updated token
                return response.request.newBuilder()
                    .header(
                        BaseInterceptor.HEADER_AUTHORIZATION_KEY,
                        prefsHelper.formatToken(updatedToken)
                    )
                    .build()
            }
            response.code == HttpURLConnection.HTTP_FORBIDDEN ||
                    response.code == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                runBlocking {
                    unlinkHandler.performUnlink()
                }
                null
            }
            else -> null
        }
    }
}
