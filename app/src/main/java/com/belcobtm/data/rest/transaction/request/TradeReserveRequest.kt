package com.belcobtm.data.rest.transaction.request

data class TradeReserveRequest(
    val type: Int,
    val fromAddress: String,
    val toAddress: String,
    val cryptoAmount: Double,
    val fee: Double,
    val hex: String
)