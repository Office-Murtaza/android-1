package com.app.belcobtm.db

import io.realm.Realm


class DbCryptoCoinModel : DbCryptoCoinInterface {
    override fun addCryptoCoin(realm: Realm, coinDb: DbCryptoCoin): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(coinDb)
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
            realm.where(DbCryptoCoin::class.java).equalTo("_ID", _ID).findFirst()?.deleteFromRealm()
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun editCryptoCoin(realm: Realm, coinDb: DbCryptoCoin): Boolean {
        return try {
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(coinDb)
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }

    override fun getCryptoCoin(realm: Realm, coinId: Int): DbCryptoCoin? {
        return realm.where(DbCryptoCoin::class.java).equalTo("_ID", coinId).findFirst()
    }

    override fun getCryptoCoin(realm: Realm, coinType: String): DbCryptoCoin? {
        return realm.where(DbCryptoCoin::class.java).equalTo("coinType", coinType).findFirst()
    }

    override fun getAllCryptoCoin(realm: Realm): ArrayList<DbCryptoCoin> {
        val list = ArrayList<DbCryptoCoin>()

        val results = realm
            .where(DbCryptoCoin::class.java)
            .findAll()
        list.addAll(realm.copyFromRealm(results))

        return list
    }

    override fun delAllCryptoCoin(realm: Realm): Boolean {
        return try {
            realm.beginTransaction()
            realm.where(DbCryptoCoin::class.java).findAll().forEach { it.deleteFromRealm() }
            realm.commitTransaction()
            true
        } catch (e: Exception) {
            println(e)
            false
        }
    }
}