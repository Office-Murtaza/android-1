package com.app.belcobtm.domain.wallet.item

class CoinFeeDataItem(
    val txFee: Double,
    val byteFee: Double,
    val gasPrice: Double,
    val gasLimit: Double,
    val profitC2C: Double,
    val walletAddress: String,
    val contractAddress: String
)