package com.app.belcobtm.data.rest.transaction.request

data class TradeRecallRequest(
    val type: Int,
    val cryptoAmount: Double
)