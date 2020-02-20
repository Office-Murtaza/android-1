package com.app.belcobtm.data.di

import android.preference.PreferenceManager
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import org.koin.dsl.module

val dataModule = module {
    single {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesHelper(sharedPreferences)
    }
    single { ApiFactory(get()) }
    single { AuthApiService((get() as ApiFactory).authApi) }
    single { NetworkUtils(get()) }
}