package com.belcobtm.data.rest.transaction.request

import com.belcobtm.domain.transaction.type.TransactionType

data class CoinToCoinExchangeRequest(
    val type: String = TransactionType.SEND_SWAP.toString(),
    val hex: String,
    val fromAddress: String?,
    val toAddress: String?,
    val cryptoAmount: Double,
    val price: Double,
    val feePercent: Double?,
    val refCoin: String,
    val refCryptoAmount: Double,
    val refCoinPrice: Double,
    val longitude: Double,
    val latitude: Double
)
