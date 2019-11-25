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
        @SerializedName("code")
        val coinCode: String,
        @SerializedName("address")
        val publicKey: String
    ){
        constructor(dbCoinDb: DbCryptoCoin):this(dbCoinDb.coinType, dbCoinDb.publicKey)
    }
}