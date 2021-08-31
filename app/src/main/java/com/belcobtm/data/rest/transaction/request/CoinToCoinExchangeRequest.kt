package com.belcobtm.data.rest.transaction.request

data class CoinToCoinExchangeRequest(
    val type: Int,
    val hex: String,
    val fromAddress: String?,
    val toAddress: String?,
    val cryptoAmount: Double,
    val price: Double,
    val serviceFee: Double?,
    val refCoin: String,
    val refCryptoAmount: Double,
    val refCoinPrice: Double,
)