package com.app.belcobtm.api.model.param

import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem
import com.google.gson.annotations.SerializedName


class AddCoinsParam(
    dbLocalCoinDbs: List<LocalCoinDataItem>
) {

    @SerializedName("coins")
    var coins: ArrayList<Coin> = ArrayList()

    init {
        dbLocalCoinDbs.forEach { coins.add(Coin(it)) }
    }

    data class Coin(
        @SerializedName("code")
        val coinCode: String,
        @SerializedName("address")
        val publicKey: String
    ) {
        constructor(dataItemLocal: LocalCoinDataItem) : this(dataItemLocal.type.name, dataItemLocal.publicKey)
    }
}