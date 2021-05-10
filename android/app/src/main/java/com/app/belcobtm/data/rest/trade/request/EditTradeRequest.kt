package com.app.belcobtm.data.rest.trade.request

data class EditTradeRequest(
    val id: String,
    val price: Double,
    val minLimit: Int,
    val maxLimit: Int,
    val paymentMethods: String,
    val terms: String,
)