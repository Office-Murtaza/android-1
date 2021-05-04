package com.app.belcobtm.presentation.features.wallet.trade.order.create.model

data class TradeOrderItem(
    val tradeId: String,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String
)