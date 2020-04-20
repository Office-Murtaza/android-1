package com.app.belcobtm.data.di

import com.app.belcobtm.data.AuthorizationRepositoryImpl
import com.app.belcobtm.data.SettingsRepositoryImpl
import com.app.belcobtm.data.WalletRepositoryImpl
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import com.app.belcobtm.domain.settings.SettingsRepository
import com.app.belcobtm.domain.wallet.WalletRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthorizationRepository> { AuthorizationRepositoryImpl(get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get(), get(), get()) }
    single<WalletRepository> { WalletRepositoryImpl(get(), get(), get()) }
}