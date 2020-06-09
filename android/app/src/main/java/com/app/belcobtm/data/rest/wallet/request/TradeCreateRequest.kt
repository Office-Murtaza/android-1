package com.app.belcobtm.data.rest.wallet.request

data class TradeCreateRequest(
    val type: Int, //1 - buy, 2 - sell
    val paymentMethod: String,
    val margin: Int,
    val minLimit: Long,
    val maxLimit: Long,
    val terms: String
)