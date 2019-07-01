package com.app.belcobtm.db

import io.realm.Realm


class CryptoCoinModel : CryptoCoinInterface {
    override fun addCryptoCoin(realm: Realm, coin: CryptoCoin): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(coin)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun delCryptoCoin(realm: Realm, _ID: Int): Boolean {
        return try {
            realm.beginTransaction()
            realm.where(CryptoCoin::class.java).equalTo("_ID", _ID).findFirst()?.deleteFromRealm()
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun editCryptoCoin(realm: Realm, coin: CryptoCoin): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(coin)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun getCryptoCoin(realm: Realm, coinId: Int): CryptoCoin? {
        return realm.where(CryptoCoin::class.java).equalTo("_ID", coinId).findFirst()
    }

    override fun getCryptoCoin(realm: Realm, coinType: String): CryptoCoin? {
        return realm.where(CryptoCoin::class.java).equalTo("coinType", coinType).findFirst()
    }

    override fun getAllCryptoCoin(realm: Realm): ArrayList<CryptoCoin> {
        val list = ArrayList<CryptoCoin>()

        val results = realm
            .where(CryptoCoin::class.java)
            .findAll()
        list.addAll(realm.copyFromRealm(results))

        return list
    }
}