package com.app.belcobtm.domain.wallet.item

data class TradeDataItem(
    val id: Int,
    val index: Int,
    val tradeCount: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val rate: Int,
    val distance: Int,
    val price: Double,
    val userName: String,
    val paymentMethod: String
)