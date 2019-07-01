package com.app.belcobtm.db

import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class CryptoCoin(

    open var coinType: String = "",
    open var publicKey: String = "",
    open var privateKey: String = ""

) : RealmObject() {
    @PrimaryKey
    open var _ID: Int = 0

    init {
        val currentIdNum = Realm.getDefaultInstance().where(CryptoCoin::class.java).max("_ID")
        _ID = if (currentIdNum == null) {
            1
        } else {
            currentIdNum.toInt() + 1
        }
    }

    fun copy(
        coinType: String = this.coinType,
        publicKey: String = this.publicKey,
        privateKey: String = this.privateKey
    ) = CryptoCoin(coinType, publicKey, privateKey)
}