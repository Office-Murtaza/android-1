package com.app.belcobtm.db

import io.realm.Realm

interface CryptoCoinInterface {
    fun addCryptoCoin(realm: Realm, coin: CryptoCoin): Boolean
    fun delCryptoCoin(realm: Realm, _ID: Int): Boolean
    fun editCryptoCoin(realm: Realm, coin: CryptoCoin): Boolean
    fun getCryptoCoin(realm: Realm, coinId: Int): CryptoCoin?
    fun getCryptoCoin(realm: Realm, coinType: String): CryptoCoin?
    fun getAllCryptoCoin(realm: Realm): ArrayList<CryptoCoin>
}