package com.app.belcobtm.data.di

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.Room
import com.app.belcobtm.data.ContactsRepositoryImpl
import com.app.belcobtm.data.cloud.auth.CloudAuth
import com.app.belcobtm.data.cloud.auth.FirebaseCloudAuth
import com.app.belcobtm.data.cloud.storage.AuthFirebaseCloudStorage
import com.app.belcobtm.data.cloud.storage.CloudStorage
import com.app.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.app.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.CHAT_STORAGE
import com.app.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.VERIFICATION_STORAGE
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.core.UnlinkHandler
import com.app.belcobtm.data.disk.AssetsDataStore
import com.app.belcobtm.data.disk.database.AppDatabase
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.helper.DistanceCalculator
import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.app.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.app.belcobtm.data.mapper.OrderResponseToOrderMapper
import com.app.belcobtm.data.mapper.TradeResponseToTradeMapper
import com.app.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.app.belcobtm.data.notification.NotificationTokenRepositoryImpl
import com.app.belcobtm.data.provider.location.LocationProvider
import com.app.belcobtm.data.provider.location.ServiceLocationProvider
import com.app.belcobtm.data.rest.atm.AtmApi
import com.app.belcobtm.data.rest.atm.AtmApiService
import com.app.belcobtm.data.rest.authorization.AuthApi
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.rest.interceptor.BaseInterceptor
import com.app.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.app.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.app.belcobtm.data.rest.interceptor.TokenAuthenticator
import com.app.belcobtm.data.rest.settings.SettingsApi
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.rest.tools.ToolsApi
import com.app.belcobtm.data.rest.tools.ToolsApiService
import com.app.belcobtm.data.rest.trade.TradeApi
import com.app.belcobtm.data.rest.trade.TradeApiService
import com.app.belcobtm.data.rest.transaction.TransactionApi
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.data.rest.wallet.WalletApi
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.domain.contacts.ContactsRepository
import com.app.belcobtm.domain.notification.NotificationTokenRepository
import com.app.belcobtm.domain.tools.IntentActions
import com.app.belcobtm.domain.tools.IntentActionsImpl
import com.app.belcobtm.presentation.core.Endpoint
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import kotlinx.coroutines.GlobalScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val dataModule = module {
    single {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesHelper(sharedPreferences)
    }
    single { BaseInterceptor(get(), get()) }
    single { NoConnectionInterceptor(get()) }
    single { ResponseInterceptor() }
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    single { AuthApiService(get()) }
    single { SettingsApiService(get()) }
    single { WalletApiService(get(), get()) }
    single { TransactionApiService(get(), get()) }
    single { ToolsApiService(get(), get()) }
    single { AtmApiService(get()) }
    single { TradeApiService(get(), get()) }
    single { NetworkUtils(get()) }
    single { AssetsDataStore(get()) }
    single { TransactionHashHelper(get(), get(), get(), get(), get()) }
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "belco_database")
            .fallbackToDestructiveMigration()
            .build()
    }
    single { Moshi.Builder().build() }
    single { (get() as AppDatabase).getCoinDao() }
    single<IntentActions> { IntentActionsImpl(get()) }
    single {
        OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .authenticator(get<TokenAuthenticator>())
            .addInterceptor(get<NoConnectionInterceptor>())
            .addInterceptor(get<BaseInterceptor>())
            .addInterceptor(get<ResponseInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(Endpoint.SERVER_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(get())
            .build()
    }
    single { get<Retrofit>().create(AtmApi::class.java) }
    single { get<Retrofit>().create(AuthApi::class.java) }
    single { get<Retrofit>().create(ToolsApi::class.java) }
    single { get<Retrofit>().create(WalletApi::class.java) }
    single { get<Retrofit>().create(SettingsApi::class.java) }
    single { get<Retrofit>().create(TransactionApi::class.java) }
    single { get<Retrofit>().create(TradeApi::class.java) }
    single<NotificationTokenRepository> { NotificationTokenRepositoryImpl(get()) }
    single<ContactsRepository> { ContactsRepositoryImpl(get<Context>().contentResolver) }
    single { Firebase.storage("gs://belco-test.appspot.com") }
    single<CloudStorage>(named(CHAT_STORAGE)) {
        AuthFirebaseCloudStorage(
            get(), get(),
            FirebaseCloudStorage(get<FirebaseStorage>().reference.child("chat"))
        )
    }
    single<CloudStorage>(named(VERIFICATION_STORAGE)) {
        AuthFirebaseCloudStorage(
            get(), get(),
            FirebaseCloudStorage(get<FirebaseStorage>().reference.child("verification"))
        )
    }
    single<CloudAuth>() { FirebaseCloudAuth(Firebase.auth) }
    single {
        TradeInMemoryCache(get(), get(), GlobalScope, get(), get(), get())
    }
    single { DistanceCalculator(get()) }
    single<LocationProvider> { ServiceLocationProvider(androidApplication()) }
    single { TransactionsInMemoryCache() }
    single { UnlinkHandler(get(), get(authenticatorQualified), get(), get()) }
    factory { TradesResponseToTradeDataMapper(get(), get(), get()) }
    factory { OrderResponseToOrderMapper() }
    factory { TradeResponseToTradeMapper() }
}
