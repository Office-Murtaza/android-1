package com.belcobtm.data.di

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.belcobtm.data.ContactsRepositoryImpl
import com.belcobtm.data.ReferralRepositoryImpl
import com.belcobtm.data.ServiceRepositoryImpl
import com.belcobtm.data.cloud.auth.CloudAuth
import com.belcobtm.data.cloud.auth.FirebaseCloudAuth
import com.belcobtm.data.cloud.storage.AuthFirebaseCloudStorage
import com.belcobtm.data.cloud.storage.CloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.CHAT_STORAGE
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage.Companion.VERIFICATION_STORAGE
import com.belcobtm.data.core.NetworkUtils
import com.belcobtm.data.core.TransactionHelper
import com.belcobtm.data.core.UnlinkHandler
import com.belcobtm.data.core.factory.BinanceTransactionInputBuilderFactory
import com.belcobtm.data.core.factory.BlockTransactionInputBuilderFactory
import com.belcobtm.data.core.factory.EthTransactionInputBuilderFactory
import com.belcobtm.data.core.factory.RippleTransactionInputBuilderFactory
import com.belcobtm.data.core.factory.TronTransactionInputBuilderFactory
import com.belcobtm.data.core.helper.BinanceTransactionHelper
import com.belcobtm.data.core.helper.BlockTransactionHelper
import com.belcobtm.data.core.helper.EthSubCoinTransactionHelper
import com.belcobtm.data.core.helper.EthTransactionHelper
import com.belcobtm.data.core.helper.RippleTransactionHelper
import com.belcobtm.data.core.helper.TronTransactionHelper
import com.belcobtm.data.disk.AssetsDataStore
import com.belcobtm.data.disk.database.AppDatabase
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_2_3
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_3_4
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_4_5
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_5_6
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_6_7
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_7_8
import com.belcobtm.data.disk.database.AppDatabase.Companion.MIGRATION_8_9
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.data.helper.DistanceCalculator
import com.belcobtm.data.inmemory.bank_accounts.BankAccountsInMemoryCache
import com.belcobtm.data.inmemory.payments.PaymentsInMemoryCache
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache
import com.belcobtm.data.inmemory.transactions.TransactionsInMemoryCache
import com.belcobtm.data.notification.NotificationTokenRepositoryImpl
import com.belcobtm.data.provider.location.LocationProvider
import com.belcobtm.data.provider.location.ServiceLocationProvider
import com.belcobtm.data.rest.atm.AtmApi
import com.belcobtm.data.rest.atm.AtmApiService
import com.belcobtm.data.rest.authorization.AuthApiService
import com.belcobtm.data.rest.bank_account.BankAccountApi
import com.belcobtm.data.rest.bank_account.BankAccountApiService
import com.belcobtm.data.rest.interceptor.BaseInterceptor
import com.belcobtm.data.rest.interceptor.NoConnectionInterceptor
import com.belcobtm.data.rest.interceptor.ResponseInterceptor
import com.belcobtm.data.rest.interceptor.TokenAuthenticator
import com.belcobtm.data.rest.referral.ReferralApi
import com.belcobtm.data.rest.referral.ReferralApiService
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
import com.belcobtm.data.support.SupportChatHelperImpl
import com.belcobtm.domain.contacts.ContactsRepository
import com.belcobtm.domain.notification.NotificationTokenRepository
import com.belcobtm.domain.referral.ReferralRepository
import com.belcobtm.domain.service.ServiceRepository
import com.belcobtm.domain.support.SupportChatHelper
import com.belcobtm.presentation.core.Endpoint
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val dataModule = module {
    single {
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
    single {
        AuthApiService(
            authApi = get(authenticatorQualified)
        )
    }
    single { SettingsApiService(get()) }
    single { BankAccountApiService(get()) }
    single { WalletApiService(get(), get()) }
    single { TransactionApiService(get(), get(), get()) }
    single { ToolsApiService(get(), get()) }
    single { AtmApiService(get()) }
    single { TradeApiService(get(), get()) }
    single { NetworkUtils(get()) }
    single {
        AssetsDataStore(
            moshi = get(),
            context = get()
        )
    }
    single { BlockTransactionInputBuilderFactory(get(), get(), get()) }
    single { BlockTransactionHelper(get()) }
    single { BinanceTransactionInputBuilderFactory(get(), get()) }
    single { BinanceTransactionHelper(get()) }
    single { RippleTransactionInputBuilderFactory(get(), get()) }
    single { RippleTransactionHelper(get()) }
    single { EthTransactionInputBuilderFactory(get(), get(), get()) }
    single { EthTransactionHelper(get()) }
    single { EthSubCoinTransactionHelper(get()) }
    single { TronTransactionInputBuilderFactory(get(), get()) }
    single { TronTransactionHelper(get()) }
    single { TransactionHelper(get(), get(), get(), get(), get(), get()) }
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "belco_database")
            .addMigrations(MIGRATION_2_3)
            .addMigrations(MIGRATION_3_4)
            .addMigrations(MIGRATION_4_5)
            .addMigrations(MIGRATION_5_6)
            .addMigrations(MIGRATION_6_7)
            .addMigrations(MIGRATION_7_8)
            .addMigrations(MIGRATION_8_9)
            .build()
    }
    single { Moshi.Builder().add(KotlinJsonAdapterFactory()).build() }
    single { get<AppDatabase>().getCoinDao() }
    single { get<AppDatabase>().getWalletDao() }
    single { get<AppDatabase>().getServiceDao() }
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
            .addConverterFactory(
                MoshiConverterFactory.create(
                    get()
                )
            )
            .client(get())
            .build()
    }
    single { get<Retrofit>().create(AtmApi::class.java) }
    single { get<Retrofit>().create(ToolsApi::class.java) }
    single { get<Retrofit>().create(WalletApi::class.java) }
    single { get<Retrofit>().create(SettingsApi::class.java) }
    single { get<Retrofit>().create(BankAccountApi::class.java) }
    single { get<Retrofit>().create(TransactionApi::class.java) }
    single { get<Retrofit>().create(TradeApi::class.java) }
    single<NotificationTokenRepository> { NotificationTokenRepositoryImpl(get()) }
    single<ContactsRepository> { ContactsRepositoryImpl(get<Context>().contentResolver) }
    single<ReferralRepository> { ReferralRepositoryImpl(get(), get(), get()) }
    single { get<Retrofit>().create(ReferralApi::class.java) }
    single { ReferralApiService(get()) }
    single { Firebase.storage("gs://belco-wallet.appspot.com") }
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
    single<CloudAuth> { FirebaseCloudAuth(Firebase.auth) }
    single {
        TradeInMemoryCache(
            distanceCalculator = get(),
            distanceCalculatorScope = GlobalScope,
            chatMessageMapper = get(),
            cacheDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
            filterDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher(),
            chatDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        )
    }
    single { DistanceCalculator(get()) }
    single<LocationProvider> {
        ServiceLocationProvider(
            androidApplication(), Executors.newSingleThreadExecutor()
        )
    }
    single<ServiceRepository> { ServiceRepositoryImpl(get()) }
    single { TransactionsInMemoryCache() }
    single { BankAccountsInMemoryCache() }
    single { PaymentsInMemoryCache() }
    single {
        UnlinkHandler(
            prefsHelper = get(),
            daoAccount = get(),
            walletDao = get(),
            context = get(),
            unlinkApi = get(authenticatorQualified),
            supportChatHelper = get()
        )
    }

    single<SupportChatHelper> {
        SupportChatHelperImpl(
            context = get(),
            prefHelper = get()
        )
    }

}
