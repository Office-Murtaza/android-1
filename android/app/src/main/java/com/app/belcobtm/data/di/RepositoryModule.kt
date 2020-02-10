package com.app.belcobtm.data.di

import com.app.belcobtm.data.AuthorizationRepositoryImpl
import com.app.belcobtm.domain.authorization.AuthorizationRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthorizationRepository> { AuthorizationRepositoryImpl(get()) }
}