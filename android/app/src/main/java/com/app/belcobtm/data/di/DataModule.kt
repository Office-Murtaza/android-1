package com.app.belcobtm.data.di

import android.preference.PreferenceManager
import androidx.room.Room
import com.app.belcobtm.data.core.FileHelper
import com.app.belcobtm.data.core.NetworkUtils
import com.app.belcobtm.data.core.TransactionHashHelper
import com.app.belcobtm.data.disk.AssetsDataStore
import com.app.belcobtm.data.disk.database.AppDatabase
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.ApiFactory
import com.app.belcobtm.data.rest.authorization.AuthApiService
import com.app.belcobtm.data.rest.settings.SettingsApiService
import com.app.belcobtm.data.rest.tools.ToolsApiService
import com.app.belcobtm.data.rest.transaction.TransactionApiService
import com.app.belcobtm.data.rest.wallet.WalletApiService
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
    single { TransactionApiService((get() as ApiFactory).transactionApi, get()) }
    single { ToolsApiService((get() as ApiFactory).toolsApi, get()) }
    single { NetworkUtils(get()) }
    single { FileHelper(get()) }
    single { AssetsDataStore(get()) }
    single { TransactionHashHelper(get(), get(), get()) }
    single { Room.databaseBuilder(get(), AppDatabase::class.java, "belco_database").build() }
    single { (get() as AppDatabase).getCoinDao() }
}