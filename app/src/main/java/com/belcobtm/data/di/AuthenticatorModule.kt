package com.belcobtm.data.di

import com.belcobtm.data.rest.authorization.AuthApi
import com.belcobtm.data.rest.interceptor.BaseInterceptor
import com.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.belcobtm.data.rest.interceptor.TokenAuthenticator
import com.belcobtm.data.rest.settings.SettingsApi
import com.belcobtm.data.rest.unlink.UnlinkApi
import com.belcobtm.presentation.core.Endpoint
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val authenticatorQualified = named("auth")

/**
 * Because of circular dependencies we ahould have
 * a different version of OkHttpClient for Authenticator
 * Circular deps: Authenticator -> ApiService -> Retrofit -> OkHttClient -> Authenticator
 * There is a copy of OkHttClient and Retrofit provision under `dataModule`
 * */
val authenticatorModule = module {
    single { TokenAuthenticator(get(authenticatorQualified), get(), get(), get()) }
    single(authenticatorQualified) {
        get<Retrofit>(authenticatorQualified).create(AuthApi::class.java)
    }
    single(authenticatorQualified) {
        get<Retrofit>(authenticatorQualified).create(UnlinkApi::class.java)
    }
    single(authenticatorQualified) {
        get<Retrofit>(authenticatorQualified).create(SettingsApi::class.java)
    }
    single(authenticatorQualified) {
        OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(get<NoConnectionInterceptor>())
            .addInterceptor(get<BaseInterceptor>())
            .addInterceptor(get<ResponseInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single(authenticatorQualified) {
        Retrofit.Builder()
            .baseUrl(Endpoint.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(get(authenticatorQualified))
            .build()
    }
}
