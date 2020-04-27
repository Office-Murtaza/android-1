package com.app.belcobtm.db

import com.app.belcobtm.domain.wallet.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.CoinTypeExtension
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import wallet.core.jni.CoinType

@Deprecated("need migrate to room")
open class DbCryptoCoin(
    open var coinType: String = "",
    open var coinTypeId: Int = -1,
    open var publicKey: String = "",
    open var privateKey: String = "",
    open var visible: Boolean = true
) : RealmObject() {
    @PrimaryKey
    open var _ID: Int = 0


    init {
        val currentIdNum = Realm.getDefaultInstance().where(DbCryptoCoin::class.java).max("_ID")
        _ID = if (currentIdNum == null) {
            1
        } else {
            currentIdNum.toInt() + 1
        }
    }

    fun copy(
        coinType: String = this.coinType,
        coinTypeId: Int = this.coinTypeId,
        publicKey: String = this.publicKey,
        privateKey: String = this.privateKey,
        visible: Boolean = this.visible
    ) = DbCryptoCoin(coinType, coinTypeId, publicKey, privateKey, visible)
}

fun DbCryptoCoin.mapToDataItem(): CoinDataItem = CoinDataItem(
    type = CoinTypeExtension.getTypeByCode(coinType)!!,
    code = coinType,
    codeId = coinTypeId,
    publicKey = publicKey,
    privateKey = privateKey,
    isVisible = visible
)