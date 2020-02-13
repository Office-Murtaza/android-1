package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.app.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.app.belcobtm.domain.authorization.interactor.VerifySmsCodeUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { ClearAppDataUseCase(get()) }
    single { RecoverWalletUseCase(get()) }
    single { VerifySmsCodeUseCase(get()) }
}