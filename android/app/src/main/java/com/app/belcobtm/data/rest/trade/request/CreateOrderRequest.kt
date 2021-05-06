package com.app.belcobtm.data.rest.trade.request

data class CreateOrderRequest(
    val tradeId: String,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val terms: String
)