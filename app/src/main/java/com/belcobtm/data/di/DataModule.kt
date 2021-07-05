package com.belcobtm.data.di

import android.content.Context
import android.preference.PreferenceManager
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.belcobtm.data.ContactsRepositoryImpl
import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.auth.FirebaseCloudAuth
import com.belcobtm.data.cloud.storage.AuthFirebaseCloudStorage
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.CHAT_STORAGE
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.VERIFICATION_STORAGE
import com.belcobtm.data.core.NetworkUtils
import com.belcobtm.data.core.TransactionHashHelper
import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.data.disk.AssetsDataStore
import com.belcobtm.data.disk.database.AppDatabase
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_2_3
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.belcobtm.data.mapper.OrderResponseToOrderMapper
import com.belcobtm.data.mapper.TradeResponseToTradeMapper
import com.belcobtm.data.mapper.TradesResponseToTradeDataMapper
import com.belcobtm.data.notification.NotificationTokenRepositoryImpl
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.provider.location.ServiceLocationProvider
import com.belcobtm.data.rest.atm.AtmApi
import com.belcobtm.data.rest.atm.AtmApiService
import com.belcobtm.data.rest.authorization.AuthApi
import com.belcobtm.data.rest.authorization.AuthApiService
import com.belcobtm.data.rest.interceptor.BaseInterceptor
import com.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.belcobtm.data.rest.interceptor.TokenAuthenticator
import com.belcobtm.data.rest.settings.SettingsApi
import com.belcobtm.data.rest.settings.SettingsApiService
import com.belcobtm.data.rest.tools.ToolsApi
import com.belcobtm.data.rest.tools.ToolsApiService
import com.belcobtm.data.rest.trade.TradeApi
import com.belcobtm.data.rest.trade.TradeApiService
import com.belcobtm.data.rest.transaction.TransactionApi
import com.belcobtm.data.rest.transaction.TransactionApiService
import com.belcobtm.data.rest.wallet.WalletApi
import com.belcobtm.data.rest.wallet.WalletApiService
import com.belcobtm.domain.contacts.ContactsRepository
import com.belcobtm.domain.notification.NotificationTokenRepository
import com.belcobtm.domain.tools.IntentActions
import com.belcobtm.domain.tools.IntentActionsImpl
import com.belcobtm.presentation.core.Endpoint
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
        // TODO can be removed after release 1.9
        PreferenceManager.getDefaultSharedPreferences(get()).edit()
            .clear()
            .apply()
        SharedPreferencesHelper(
            EncryptedSharedPreferences.create(
                "belco_secret_prefs",
                MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                get(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        )
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
            .addMigrations(MIGRATION_2_3)
            .build()
    }
    single { Moshi.Builder().build() }
    single { get<AppDatabase>().getCoinDao() }
    single { get<AppDatabase>().getWalletDao() }
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
    single { UnlinkHandler(get(), get(), get(), get()) }
    factory { TradesResponseToTradeDataMapper(get(), get(), get()) }
    factory { OrderResponseToOrderMapper() }
    factory { TradeResponseToTradeMapper() }
}