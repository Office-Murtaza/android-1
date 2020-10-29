package com.app.belcobtm

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.app.belcobtm.data.di.dataModule
import com.app.belcobtm.data.di.repositoryModule
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.sockets.SocketClient
import com.app.belcobtm.presentation.di.useCaseModule
import com.app.belcobtm.presentation.di.viewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : MultiDexApplication(), LifecycleObserver {
    private val prefHelper: SharedPreferencesHelper by inject()
    private val socketClient: SocketClient by inject()
    private var loggedIn = false

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        System.loadLibrary("TrustWalletCore")

        startKoin {
            modules(
                listOf(
                    dataModule,
                    repositoryModule,
                    useCaseModule,
                    viewModelModule
                )
            )

            androidContext(applicationContext)
        }
        prefHelper.coinsDetails = emptyMap()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        socketClient.disconnect()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        connect()
    }

    fun onLogin() {
        loggedIn = true
        connect()
    }

    fun onLogout() {
        loggedIn = false
        disconnect()
    }

    private fun connect() {
        //todo uncomment after release
//        if (!socketClient.isConnected() && loggedIn) {
//            socketClient.connect()
//        }
    }

    private fun disconnect() {
        //todo uncomment after release
//        socketClient.disconnect()
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }
}
