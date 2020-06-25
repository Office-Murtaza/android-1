package com.app.belcobtm.data.rest.transaction.request

data class CoinToCoinExchangeRequest(
    val type: Int,
    val cryptoAmount: Double,
    val refCoin: String,
    val hex: String
)