package com.app.belcobtm.api.model.param

import com.app.belcobtm.domain.wallet.item.AccountDataItem
import com.google.gson.annotations.SerializedName


class AddCoinsParam(
    dbAccountDbs: List<AccountDataItem>
) {

    @SerializedName("coins")
    var coins: ArrayList<Coin> = ArrayList()

    init {
        dbAccountDbs.forEach { coins.add(Coin(it)) }
    }

    data class Coin(
        @SerializedName("code")
        val coinCode: String,
        @SerializedName("address")
        val publicKey: String
    ) {
        constructor(dataItemLocal: AccountDataItem) : this(dataItemLocal.type.name, dataItemLocal.publicKey)
    }
}