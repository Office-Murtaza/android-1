package com.belcobtm.data.di

import com.belcobtm.data.AccountRepositoryImpl
import com.belcobtm.data.AtmRepositoryImpl
import com.belcobtm.data.AuthorizationRepositoryImpl
import com.belcobtm.data.BankAccountRepositoryImpl
import com.belcobtm.data.SettingsRepositoryImpl
import com.belcobtm.data.ToolsRepositoryImpl
import com.belcobtm.data.TradeRepositoryImpl
import com.belcobtm.data.TransactionRepositoryImpl
import com.belcobtm.data.WalletRepositoryImpl
import com.belcobtm.data.cloud.storage.FirebaseCloudStorage
import com.belcobtm.data.disk.database.AppDatabase
import com.belcobtm.domain.account.AccountRepository
import com.belcobtm.domain.atm.AtmRepository
import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.bank_account.BankAccountRepository
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.tools.ToolsRepository
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.wallet.WalletRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.core.qualifier.named
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthorizationRepository> {
        AuthorizationRepositoryImpl(
            prefHelper = get(),
            apiService = get(),
            accountDao = (get() as AppDatabase).getCoinDao(),
            walletDao = get(),
            serviceRepository = get(),
            locationProvider = get()
        )
    }
    single<BankAccountRepository> {
        BankAccountRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            androidApplication(),
            get(),
            get(),
            get(),
            get(named(FirebaseCloudStorage.VERIFICATION_STORAGE))
        )
    }
    single<WalletRepository> { WalletRepositoryImpl(get(), get()) }
    single<AccountRepository> { AccountRepositoryImpl(get(), get()) }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<ToolsRepository> { ToolsRepositoryImpl(get()) }
    single<AtmRepository> { AtmRepositoryImpl(get()) }
    single<TradeRepository> {
        TradeRepositoryImpl(
            get(),
            get(),
            get(),
            androidApplication().resources,
            get()
        )
    }

}
