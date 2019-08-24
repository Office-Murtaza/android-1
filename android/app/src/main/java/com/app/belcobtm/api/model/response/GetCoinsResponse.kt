package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName
import org.parceler.Parcel


data class GetCoinsResponse(
    @SerializedName("coins")
    val coins: List<CoinModel>,
    @SerializedName("totalBalance")
    val totalBalance: TotalBalance,
    @SerializedName("userId")
    val userId: Int // 1000007
) {
    data class TotalBalance(
        @SerializedName("USD")
        val uSD: Double // 13055.20
    )
}

@Parcel
class CoinModel {
    constructor()

    constructor(
        balance: Double,
        coinId: String,
        orderIndex: Int,
        price: Price,
        publicKey: String
    ) {
        this.balance = balance
        this.coinId = coinId
        this.orderIndex = orderIndex
        this.price = price
        this.publicKey = publicKey
    }


    @SerializedName("balance")
    var balance: Double = 0.0 // 1
    @SerializedName("coinId")
    var coinId: String = ""// XRP
    @SerializedName("orderIndex")
    var orderIndex: Int = -1 // 6
    @SerializedName("price")
    var price: Price = Price()
    @SerializedName("publicKey")
    var publicKey: String = ""

    val fullCoinName:String
    get() {
        when(coinId){
            "BTC"-> return "Bitcoin"
            "BCH"-> return "Bitcoin Cash"
            "ETH"-> return "Ethereum"
            "LTC"-> return "Litecoin"
            "BNB"-> return "Binance"
            "TRX"-> return "TRON"
            "XRP"-> return "Ripple"
            else-> return "Unknown"
        }

    }

    // rEdQsbTqrQbhT1wourGZqk9c5rjfKm51Wp
    @Parcel
    class Price {
        constructor()

        constructor(
            uSD: Double
        ) {
            this.uSD = uSD
        }

        @SerializedName("USD")
        var uSD: Double = 0.0 // 0.35
    }
}
