package com.app.belcobtm.data.rest.trade.request

data class TradeRequest(
    val type: Int,
    val coinCode: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val paymentMethods: String,
    val terms: String,
    val makerLatitude: Double,
    val makerLongitude: Double
)