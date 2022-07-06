package com.belcobtm.data.rest.trade.request

data class CreateTradeRequest(
    val type: String,
    val coin: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val paymentMethods: String,
    val terms: String,
    val feePercent: Double,
    val fiatAmount: Double,
    val longitude: Double,
    val latitude: Double
)