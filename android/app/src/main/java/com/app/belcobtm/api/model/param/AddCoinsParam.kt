package com.app.belcobtm.api.model.param

import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.google.gson.annotations.SerializedName


class AddCoinsParam(
    dbCoinDbs: List<CoinDataItem>
) {

    @SerializedName("coins")
    var coins: ArrayList<Coin> = ArrayList()

    init {
        dbCoinDbs.forEach { coins.add(Coin(it)) }
    }

    data class Coin(
        @SerializedName("code")
        val coinCode: String,
        @SerializedName("address")
        val publicKey: String
    ) {
        constructor(dataItem: CoinDataItem) : this(dataItem.type.name, dataItem.publicKey)
    }
}