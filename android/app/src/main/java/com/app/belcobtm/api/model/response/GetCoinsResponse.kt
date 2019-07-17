package com.app.belcobtm.api.model.response

import com.google.gson.annotations.SerializedName


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

    data class CoinModel(
        @SerializedName("balance")
        val balance: Double, // 1
        @SerializedName("coinId")
        val coinId: String, // XRP
        @SerializedName("orderIndex")
        val orderIndex: Int, // 6
        @SerializedName("price")
        val price: Price,
        @SerializedName("publicKey")
        val publicKey: String // rEdQsbTqrQbhT1wourGZqk9c5rjfKm51Wp
    ) {
        data class Price(
            @SerializedName("USD")
            val uSD: Double // 0.35
        )
    }
}
