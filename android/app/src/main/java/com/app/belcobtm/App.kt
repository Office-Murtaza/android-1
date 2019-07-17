package com.app.belcobtm

import android.app.Activity
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.app.belcobtm.di.component.DaggerAppComponent
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class App @Inject constructor() : MultiDexApplication(), HasActivityInjector {

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("TrustWalletCore")

        DaggerAppComponent
            .builder()
            .application(this)
            .build()
            .inject(this)

        Realm.init(this)
        val config = RealmConfiguration.Builder()
        config.name("crypto_coin")
        config.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(config.build())
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }
}