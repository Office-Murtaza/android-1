package com.app.belcobtm.data.di

import com.app.belcobtm.data.*
import com.app.belcobtm.data.disk.database.AppDatabase
import com.app.belcobtm.domain.atm.AtmRepository
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.tools.ToolsRepository
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.wallet.WalletRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthorizationRepository> {
        AuthorizationRepositoryImpl(
            get(),
            get(),
            (get() as AppDatabase).getCoinDao()
        )
    }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get(), get(), get()) }
    single<WalletRepository> { WalletRepositoryImpl(get(), get(), get(), get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get(), get(), get()) }
    single<ToolsRepository> { ToolsRepositoryImpl(get(), get()) }
    single<AtmRepository> { AtmRepositoryImpl(get(), get()) }
}