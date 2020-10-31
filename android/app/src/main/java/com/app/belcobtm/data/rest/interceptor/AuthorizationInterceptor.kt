package com.app.belcobtm.data.rest.interceptor

import com.app.belcobtm.data.core.getJSONFromBody
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.request.RefreshTokenRequest
import com.app.belcobtm.data.rest.authorization.response.AuthorizationResponse
import com.squareup.moshi.Moshi
import okhttp3.*
import java.net.HttpURLConnection

/**
 * Interceptor responsible for handling HttpURLConnection.HTTP_UNAUTHORIZED status code
 * */
class AuthorizationInterceptor(
    private val moshi: Moshi,
    private val prefsHelper: SharedPreferencesHelper,
    private val responseInterceptor: ResponseInterceptor
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val procesedResponse = chain.proceed(request)
        if (procesedResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
            val refereshTokenResponse = performRefreshToken(chain)
            if (refereshTokenResponse.code() == HttpURLConnection.HTTP_OK) {
                mapRefreshTokenResponseToModel(refereshTokenResponse)?.let { authModel ->
                    updateTokenData(authModel)
                    val updatedToken = authModel.accessToken
                    val requestBuilder = request.newBuilder()
                    return performRequestWithToken(updatedToken, chain, requestBuilder)
                }
            }
        }
        return procesedResponse
    }

    private fun performRefreshToken(chain: Interceptor.Chain): Response {
        val request = Request.Builder()
            .url("${ApiFactory.SERVER_URL}refresh")
            .method("POST", createRefreshTokenBody())
            .build()
        return chain.proceed(request)
    }

    private fun createRefreshTokenBody(): RequestBody {
        val refreshToken = prefsHelper.refreshToken
        val refreshTokenData = RefreshTokenRequest(refreshToken)
        val adapter = moshi.adapter(RefreshTokenRequest::class.java)
        val refereshRequestJson = adapter.toJson(refreshTokenData)
        val mediaType = MediaType.get("application/json; charset=utf-8")
        return RequestBody.create(mediaType, refereshRequestJson)
    }

    private fun mapRefreshTokenResponseToModel(respone: Response): AuthorizationResponse? {
        val extractedResponse = responseInterceptor.extractResponse(respone)
        val json = extractedResponse.getJSONFromBody()
        val adapter = moshi.adapter(AuthorizationResponse::class.java)
        return adapter.fromJson(json)
    }

    private fun updateTokenData(authModel: AuthorizationResponse) {
        prefsHelper.processAuthResponse(authModel)
    }

    private fun performRequestWithToken(
        accessToken: String,
        chain: Interceptor.Chain,
        requestBuilder: Request.Builder
    ): Response {
        val request = requestBuilder
            .removeHeader(BaseInterceptor.HEADER_AUTHORIZATION_KEY)
            .addHeader(BaseInterceptor.HEADER_AUTHORIZATION_KEY, accessToken)
            .build()
        val response = chain.proceed(request)
        // we need to make sure that new response is `extracted` from response object
        return responseInterceptor.extractResponse(response)
    }
}
