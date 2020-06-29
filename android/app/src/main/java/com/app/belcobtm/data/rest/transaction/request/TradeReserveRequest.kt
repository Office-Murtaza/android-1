package com.app.belcobtm.data.rest.transaction.request

data class TradeReserveRequest(
    val type: Int,
    val cryptoAmount: Double,
    val hex: String
)