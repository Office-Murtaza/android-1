package com.app.belcobtm

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.multidex.MultiDexApplication
import com.app.belcobtm.data.di.dataModule
import com.app.belcobtm.data.di.repositoryModule
import com.app.belcobtm.data.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.di.component.DaggerAppComponent
import com.app.belcobtm.presentation.di.useCaseModule
import com.app.belcobtm.presentation.di.viewModelModule
import com.facebook.stetho.Stetho
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.support.HasSupportFragmentInjector
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class App
@Inject constructor() : MultiDexApplication(), HasActivityInjector, HasSupportFragmentInjector {
    private val prefHelper: SharedPreferencesHelper by inject()

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var fragmentDispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
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

        Realm.init(this)
        val config = RealmConfiguration.Builder()
        config.name("crypto_coin")
        config.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(config.build())

        Stetho.initializeWithDefaults(this)
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingAndroidInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentDispatchingAndroidInjector
    }


    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }
    }
}