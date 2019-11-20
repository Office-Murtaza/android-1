package com.app.belcobtm.api

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class TempUtxoRetrofitClient private constructor() {
    private object Holder {
        val INSTANCE = TempUtxoRetrofitClient()
    }

    companion object {
        val instance: TempUtxoRetrofitClient by lazy { TempUtxoRetrofitClient.Holder.INSTANCE }
    }

    private val TEMP_API_URL = "http://167.99.144.115:9134/api/v2/"

    var apiInterface: TempUtxoApiInterface = getClient(TEMP_API_URL).create(TempUtxoApiInterface::class.java)

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
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Requested-With", "XMLHttpRequest")
                    .addHeader("Accept", "application/json")
                chain.proceed(request.build())
            }
            .addInterceptor(httpLoggingInterceptor)
            .build()

    }
}