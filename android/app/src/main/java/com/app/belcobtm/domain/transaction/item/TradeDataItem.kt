package com.app.belcobtm.domain.transaction.item

data class TradeDataItem(
    val id: Int,
    val index: Int,
    val tradeCount: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val distance: Int,
    val rate: Double,
    val price: Double,
    val userName: String,
    val paymentMethod: String,
    val terms: String
)