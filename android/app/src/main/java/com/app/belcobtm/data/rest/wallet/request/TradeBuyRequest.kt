package com.app.belcobtm.data.rest.wallet.request

data class TradeBuyRequest(
    val tradeId: Int,
    val price: Int,
    val fiatAmount: Int,
    val cryptoAmount: Double,
    val details: String
)