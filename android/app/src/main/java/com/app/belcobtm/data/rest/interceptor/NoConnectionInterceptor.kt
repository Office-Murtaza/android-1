package com.app.belcobtm.data.rest.interceptor

import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.domain.Failure
import okhttp3.Interceptor
import okhttp3.Response

class NoConnectionInterceptor(private val networkUtils: NetworkUtils) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return when (networkUtils.isNetworkAvailable()) {
            true -> chain.proceed(chain.request())
            false -> throw Failure.NetworkConnection
        }
    }
}
