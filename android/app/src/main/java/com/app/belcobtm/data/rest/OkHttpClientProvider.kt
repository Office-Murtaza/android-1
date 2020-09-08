package com.app.belcobtm.data.rest

import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.LogInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class OkHttpClientProvider() {

    private val WAIT_TIME_SECONDS = 60L

    fun provideOkHttpClient(
        noConnectionInterceptor: NoConnectionInterceptor,
        baseInterceptor: BaseInterceptor,
        responseInterceptor: ResponseInterceptor,
        logInterceptor: LogInterceptor
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .connectTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
            .readTimeout(WAIT_TIME_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(noConnectionInterceptor)
            .addInterceptor(baseInterceptor)
            .addInterceptor(logInterceptor)
            .addInterceptor(responseInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
}