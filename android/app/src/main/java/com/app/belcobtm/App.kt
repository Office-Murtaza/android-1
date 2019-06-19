package com.app.belcobtm

import android.content.Context
import android.support.multidex.MultiDexApplication
import io.realm.Realm
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class App @Inject constructor() : MultiDexApplication() {

    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }

//        val pref: PrefManager
//            get() = PrefManager.getInstance(instance!!.applicationContext)
    }

//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        System.loadLibrary("TrustWalletCore")
//        DaggerAppComponent
//            .builder()
//            .application(this)
//            .build()
//            .inject(this)
    }

//    override fun activityInjector(): AndroidInjector<Activity>? {
//        return dispatchingAndroidInjector
//    }
}