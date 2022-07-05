package com.belcobtm

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.belcobtm.data.di.authenticatorModule
import com.belcobtm.data.di.dataModule
import com.belcobtm.data.di.repositoryModule
import com.belcobtm.data.di.webSocketModule
import com.belcobtm.presentation.core.Const
import com.belcobtm.presentation.di.helperModule
import com.belcobtm.presentation.di.useCaseModule
import com.belcobtm.presentation.di.viewModelHelperModule
import com.belcobtm.presentation.di.viewModelModule
import com.giphy.sdk.ui.Giphy
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        System.loadLibrary("TrustWalletCore")
        Giphy.configure(applicationContext, Const.GIPHY_API_KEY)

        startKoin {
            androidContext(applicationContext)
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
        }
    }

    companion object {

        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }

}
