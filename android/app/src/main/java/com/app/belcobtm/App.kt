package com.app.belcobtm

import android.content.Context
import androidx.multidex.MultiDexApplication
import io.realm.Realm
import io.realm.RealmConfiguration
import org.spongycastle.asn1.x500.style.RFC4519Style.c
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class App @Inject constructor() : MultiDexApplication() {

    init {
        instance = this
//        mDaoSession = AbstractDaoSession(
//            AbstractDaoMaster.DevOpenHelper(this, "greendao_demo.db").getWritableDb()
//        ).newSession()
    }

    companion object {
        private var instance: App? = null

        fun appContext(): Context {
            return instance!!.applicationContext
        }

//        private val mDaoSession: DaoSession
//        fun getDaoSession(): DaoSession {
//            return mDaoSession
//
//           val  helper :AbstractDaoMaster.DevOpenHelper = new DaoMaster.DevOpenHelper(this, "notes-db");
//            Database db = helper.getWritableDb();
//            daoSession = new DaoMaster(db).newSession();
//        }


//        val pref: PrefManager
//            get() = PrefManager.getInstance(instance!!.applicationContext)
    }

//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("TrustWalletCore")
//        DaggerAppComponent
//            .builder()
//            .application(this)
//            .build()
//            .inject(this)
        Realm.init(this)
        val config = RealmConfiguration.Builder()
        config.name("crypto_coin")
        config.deleteRealmIfMigrationNeeded()
        Realm.setDefaultConfiguration(config.build())
    }

//    override fun activityInjector(): AndroidInjector<Activity>? {
//        return dispatchingAndroidInjector
//    }
}