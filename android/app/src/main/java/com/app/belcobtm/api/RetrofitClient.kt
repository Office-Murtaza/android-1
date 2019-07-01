package com.app.belcobtm.api

import com.app.belcobtm.App
import com.app.belcobtm.util.Const.API_URL
import com.app.belcobtm.util.pref
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by ADMIN on 17.07.2018.
 */
class RetrofitClient private constructor() {
    private object Holder {
        val INSTANCE = RetrofitClient()
    }

    companion object {
        val instance: RetrofitClient by lazy { Holder.INSTANCE }
    }

    lateinit var apiInterface: ApiInterface

    init {
        initApiInterface()
    }

    //public for reinit ApiInterface after token getting
    fun initApiInterface() {
        apiInterface = getClient(API_URL).create(ApiInterface::class.java)
//        apiInterface = getClient(REST_API_URL_DEV).create(ApiInterface::class.java)
    }


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

        val token = App.appContext().pref.getSessionApiToken()
        if (token.isNullOrEmpty()) {
            return OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(ErrorInterceptor())
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .addHeader("Accept", "application/json")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(httpLoggingInterceptor)
                .build()
        } else {
            return OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(ErrorInterceptor())
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Content-Type", "application/json")
                        .addHeader("X-Requested-With", "XMLHttpRequest")
                        .addHeader("Accept", "application/json")
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                    chain.proceed(request)
                }
                .addInterceptor(httpLoggingInterceptor)
                .build()
        }
    }
}