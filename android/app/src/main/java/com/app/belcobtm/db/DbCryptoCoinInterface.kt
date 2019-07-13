package com.app.belcobtm.db

import io.realm.Realm

interface DbCryptoCoinInterface {
    fun addCryptoCoin(realm: Realm, coinDb: DbCryptoCoin): Boolean
    fun delCryptoCoin(realm: Realm, _ID: Int): Boolean
    fun editCryptoCoin(realm: Realm, coinDb: DbCryptoCoin): Boolean
    fun getCryptoCoin(realm: Realm, coinId: Int): DbCryptoCoin?
    fun getCryptoCoin(realm: Realm, coinType: String): DbCryptoCoin?
    fun getAllCryptoCoin(realm: Realm): ArrayList<DbCryptoCoin>
    fun delAllCryptoCoin(realm: Realm): Boolean

}