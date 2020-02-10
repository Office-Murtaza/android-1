package com.app.belcobtm.presentation.di

import com.app.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { ClearAppDataUseCase(get()) }
}