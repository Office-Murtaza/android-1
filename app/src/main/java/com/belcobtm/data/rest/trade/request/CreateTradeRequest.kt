package com.belcobtm.data.rest.trade.request

data class CreateTradeRequest(
    val type: String,
    val coin: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val fiatAmount: Double,
    val feePercent: Double,
    val paymentMethods: List<String>,
    val terms: String,
    val longitude: Double,
    val latitude: Double
)