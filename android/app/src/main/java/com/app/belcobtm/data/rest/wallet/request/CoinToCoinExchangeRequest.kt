package com.app.belcobtm.data.rest.wallet.request

data class CoinToCoinExchangeRequest(
    val type: Int,
    val cryptoAmount: Double,
    val refCoin: String,
    val hex: String
)