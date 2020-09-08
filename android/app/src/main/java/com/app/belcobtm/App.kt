package com.app.belcobtm

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.app.belcobtm.data.di.dataModule
import com.app.belcobtm.data.di.repositoryModule
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.sockets.SocketClient
import com.app.belcobtm.di.component.DaggerAppComponent
import com.app.belcobtm.presentation.di.useCaseModule
import com.app.belcobtm.presentation.di.viewModelModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class App
@Inject constructor() : MultiDexApplication(), HasActivityInjector, HasSupportFragmentInjector, LifecycleObserver {
    private val prefHelper: SharedPreferencesHelper by inject()
    private val socketClient: SocketClient by inject()
    private var loggedIn = false

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

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
        prefHelper.coinsFee = emptyMap()

        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
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
        if (!socketClient.isConnected() && loggedIn) {
            socketClient.connect()
        }
    }

    private fun disconnect() {
        socketClient.disconnect()
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }
}