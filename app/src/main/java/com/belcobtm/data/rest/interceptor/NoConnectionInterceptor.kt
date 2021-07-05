package com.belcobtm.data.rest.interceptor

import com.belcobtm.data.core.NetworkUtils
import com.belcobtm.domain.Failure
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
