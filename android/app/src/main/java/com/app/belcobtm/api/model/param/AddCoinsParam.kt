package com.app.belcobtm.api.model.param

import com.app.belcobtm.db.DbCryptoCoin
import com.google.gson.annotations.SerializedName


 class AddCoinsParam(
     dbCoinDbs: ArrayList<DbCryptoCoin>
) {

    @SerializedName("coins")
    var coins: ArrayList<Coin> = ArrayList()

     init {
         dbCoinDbs.forEach{coins.add(Coin(it))}
     }

    data class Coin(
        @SerializedName("coinCode")
        val coinCode: String, // XRP
        @SerializedName("publicKey")
        val publicKey: String // 111111111111dddddddddddddd
    ){
        constructor(dbCoinDb: DbCryptoCoin):this(dbCoinDb.coinType, dbCoinDb.publicKey)
    }
}