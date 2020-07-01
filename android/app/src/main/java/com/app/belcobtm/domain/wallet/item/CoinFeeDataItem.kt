package com.app.belcobtm.domain.wallet.item

class CoinFeeDataItem(
    val txFee: Double,
    val byteFee: Double,
    val gasPrice: Double,
    val gasLimit: Double,
    val profitExchange: Double,
    val walletAddress: String, //server address
    val contractAddress: String
)