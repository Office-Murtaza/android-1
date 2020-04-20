package com.app.belcobtm.data.di

import android.preference.PreferenceManager
import com.app.belcobtm.data.TransactionHashHelper
import com.app.belcobtm.data.core.FileHelper
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.disk.AssetsDataStore
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.rest.wallet.WalletApi
import com.app.belcobtm.data.rest.wallet.WalletApiService
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import org.koin.dsl.module

val dataModule = module {
    single {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(get())
        SharedPreferencesHelper(sharedPreferences)
    }
    single { ApiFactory(get()) }
    single { AuthApiService((get() as ApiFactory).authApi) }
    single { SettingsApiService(get(), (get() as ApiFactory).settingsApi) }
    single { WalletApiService((get() as ApiFactory).walletApi, get()) }
    single { NetworkUtils(get()) }
    single { FileHelper(get()) }
    single { AssetsDataStore(get()) }
    single { TransactionHashHelper(get(), get()) }
}