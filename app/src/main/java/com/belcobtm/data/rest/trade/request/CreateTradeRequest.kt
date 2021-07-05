package com.belcobtm.data.rest.trade.request

data class CreateTradeRequest(
    val type: Int,
    val coin: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val paymentMethods: String,
    val terms: String
)