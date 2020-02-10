package com.app.belcobtm.presentation.di

import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { WelcomeViewModel(get()) }
}