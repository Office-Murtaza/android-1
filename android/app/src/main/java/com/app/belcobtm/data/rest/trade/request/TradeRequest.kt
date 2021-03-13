package com.app.belcobtm.data.rest.trade.request

data class TradeRequest(
    val type: Int,
    val coin: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val paymentMethods: String,
    val terms: String,
    val makerLatitude: Double? = null,
    val makerLongitude: Double? = null
)