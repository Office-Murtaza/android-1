package com.app.belcobtm

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.app.belcobtm.data.di.authenticatorModule
import com.app.belcobtm.data.di.dataModule
import com.app.belcobtm.data.di.repositoryModule
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.presentation.di.useCaseModule
import com.app.belcobtm.presentation.di.viewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : Application() {
    private val prefHelper: SharedPreferencesHelper by inject()

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        System.loadLibrary("TrustWalletCore")

        startKoin {
            modules(
                listOf(
                    dataModule,
                    authenticatorModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule
                )
            )

            androidContext(applicationContext)
        }
        prefHelper.coinsDetails = emptyMap()
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }
}
