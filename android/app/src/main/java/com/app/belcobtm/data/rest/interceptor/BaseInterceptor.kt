package com.app.belcobtm.data.rest.interceptor

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.Failure
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class BaseInterceptor(
    private val prefHelper: SharedPreferencesHelper,
    private val networkUtils: NetworkUtils
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        return try {
            val request = chain.request()
                .newBuilder()
                .addHeader(HEADER_CONTENT_TYPE_KEY, HEADER_CONTENT_TYPE_VALUE)
                .addHeader(HEADER_X_REQUESTED_WITH_KEY, HEADER_X_REQUESTED_WITH_VALUE)
                .addHeader(HEADER_ACCEPT_KEY, HEADER_ACCEPT_VALUE)
                .addHeader(HEADER_AUTHORIZATION_KEY, prefHelper.accessToken)
                .build()
            chain.proceed(request)
        } catch (e: Exception) {
            if (networkUtils.isNetworkAvailable()) {
                throw e
            } else {
                throw Failure.NetworkConnection
            }
        }
    }

    companion object {
        private const val HEADER_CONTENT_TYPE_KEY = "Content-Type"
        private const val HEADER_CONTENT_TYPE_VALUE = "application/json"
        private const val HEADER_X_REQUESTED_WITH_KEY = "X-Requested-With"
        private const val HEADER_X_REQUESTED_WITH_VALUE = "XMLHttpRequest"
        private const val HEADER_ACCEPT_KEY = "Accept"
        private const val HEADER_ACCEPT_VALUE = "application/json"
        const val HEADER_AUTHORIZATION_KEY = "Authorization"
    }
}