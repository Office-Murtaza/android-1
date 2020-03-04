package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.authorization.interactor.*
import com.app.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { ClearAppDataUseCase(get()) }
    single { RecoverWalletUseCase(get()) }
    single { RecoverWalletVerifySmsCodeUseCase(get()) }
    single { CreateWalletUseCase(get()) }
    single { CreateWalletVerifySmsCodeUseCase(get()) }
    single { AuthorizeUseCase(get()) }
    single { GetAuthorizePinUseCase(get()) }
    single { SaveAuthorizePinUseCase(get()) }
    single { GetVerificationInfoUseCase(get()) }
}