package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.authorization.interactor.*
import org.koin.dsl.module

val useCaseModule = module {
    single { ClearAppDataUseCase(get()) }
    single { RecoverWalletUseCase(get()) }
    single { RecoverWalletVerifySmsCodeUseCase(get()) }
    single { CreateWalletUseCase(get()) }
    single { CreateWalletVerifySmsCodeUseCase(get()) }
}