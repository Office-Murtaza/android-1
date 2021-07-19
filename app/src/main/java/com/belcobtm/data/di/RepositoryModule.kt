package com.belcobtm.data.di

import com.belcobtm.data.*
import com.belcobtm.data.disk.database.AppDatabase
import com.belcobtm.domain.account.AccountRepository
import com.belcobtm.domain.atm.AtmRepository
import com.belcobtm.domain.authorization.AuthorizationRepository
import com.belcobtm.domain.settings.SettingsRepository
import com.belcobtm.domain.tools.ToolsRepository
import com.belcobtm.domain.trade.TradeRepository
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.wallet.WalletRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthorizationRepository> {
        AuthorizationRepositoryImpl(
            androidApplication(),
            get(),
            get(),
            (get() as AppDatabase).getCoinDao(),
            get()
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(androidApplication(), get(), get(), get()) }
    single<WalletRepository> { WalletRepositoryImpl(get(), get()) }
    single<AccountRepository> { AccountRepositoryImpl(get(), get()) }
    single<TransactionRepository> {
        TransactionRepositoryImpl(
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