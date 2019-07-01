package com.app.belcobtm.api.model.param

import com.app.belcobtm.db.CryptoCoin
import com.google.gson.annotations.SerializedName


 class AddCoinsParam(
    dbCoins: ArrayList<CryptoCoin>,
    @SerializedName("userId")
    val userId: String // 1000001
) {

    @SerializedName("coins")
    var coins: ArrayList<Coin> = ArrayList()

     init {
         dbCoins.forEach{coins.add(Coin(it))}
     }

    data class Coin(
        @SerializedName("coinCode")
        val coinCode: String, // XRP
        @SerializedName("publicKey")
        val publicKey: String // 111111111111dddddddddddddd
    ){
        constructor(dbCoin: CryptoCoin):this(dbCoin.coinType, dbCoin.publicKey)
    }
}