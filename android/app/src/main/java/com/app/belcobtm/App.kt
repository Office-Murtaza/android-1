package com.app.belcobtm

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.app.belcobtm.data.di.*
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.presentation.core.Const
import com.app.belcobtm.presentation.di.helperModule
import com.app.belcobtm.presentation.di.useCaseModule
import com.app.belcobtm.presentation.di.viewModelHelperModule
import com.app.belcobtm.presentation.di.viewModelModule
import com.giphy.sdk.ui.Giphy
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    private val prefHelper: SharedPreferencesHelper by inject()
    private val walletLifecycleObserver: LifecycleObserver by inject(
        WALLET_LIFECYCLE_OBSERVER_QUALIFIER
    )

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        System.loadLibrary("TrustWalletCore")
        Giphy.configure(applicationContext, Const.GIPHY_API_KEY)

        startKoin {
            modules(
                listOf(
                    dataModule,
                    authenticatorModule,
                    repositoryModule,
                    webSocketModule,
                    useCaseModule,
                    viewModelModule,
                    viewModelHelperModule,
                    helperModule
                )
            )
            androidContext(applicationContext)
        }
        // TODO token cleanup can be removed after force update
        prefHelper.clearValue(SharedPreferencesHelper.ACCESS_TOKEN)
        ProcessLifecycleOwner.get().lifecycle.addObserver(walletLifecycleObserver)
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }
}
