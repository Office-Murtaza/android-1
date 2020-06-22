package com.app.belcobtm.data.rest.transaction.request

data class TradeCreateRequest(
    val type: Int, //1 - buy, 2 - sell
    val paymentMethod: String,
    val margin: Double,
    val minLimit: Long,
    val maxLimit: Long,
    val terms: String
)