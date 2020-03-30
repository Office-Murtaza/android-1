package com.app.belcobtm.api

import com.app.belcobtm.App
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.presentation.core.pref
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient private constructor() {
    private object Holder {
        val INSTANCE = RetrofitClient()
    }

    companion object {
        val instance: RetrofitClient by lazy { Holder.INSTANCE }
    }

    var apiInterface: ApiInterface = getClient(ApiFactory.SERVER_URL).create(ApiInterface::class.java)

    private fun getClient(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(getHttpClient())
            .build()
    }

    private fun getHttpClient(): OkHttpClient {
        val httpLoggingInterceptor: HttpLoggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)

        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(ErrorInterceptor())
            .addNetworkInterceptor(StethoInterceptor())
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Accept", "application/json")
                val refreshToken = App.appContext().pref.getSessionApiToken()
                if (!refreshToken.isNullOrBlank())
                    request.addHeader("Authorization", "Bearer $refreshToken")
                chain.proceed(request.build())
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()

    }
}