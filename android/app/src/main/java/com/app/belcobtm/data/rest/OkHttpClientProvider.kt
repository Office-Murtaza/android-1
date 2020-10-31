package com.app.belcobtm.data.rest

import com.app.belcobtm.data.rest.interceptor.AuthorizationInterceptor
import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpClientProvider {

    companion object {
        private const val WAIT_TIME_SECONDS: Long = 60
    }

    fun provideOkHttpClient(
        noConnectionInterceptor: NoConnectionInterceptor,
        baseInterceptor: BaseInterceptor,
        responseInterceptor: ResponseInterceptor,
        authInterceptor: AuthorizationInterceptor
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .connectTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
            .readTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(noConnectionInterceptor)
            .addInterceptor(baseInterceptor)
            .addInterceptor(responseInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
}