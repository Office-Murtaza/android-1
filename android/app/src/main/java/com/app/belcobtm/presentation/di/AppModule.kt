package com.app.belcobtm.presentation.di

import com.app.belcobtm.presentation.features.authorization.pin.PinViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.create.CreateWalletViewModel
import com.app.belcobtm.presentation.features.authorization.wallet.recover.RecoverWalletViewModel
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WelcomeViewModel(get()) }
    viewModel { RecoverWalletViewModel(get(), get()) }
    viewModel { CreateWalletViewModel(get(), get()) }
    viewModel { PinViewModel(get(), get(), get()) }
}