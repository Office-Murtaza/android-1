package com.app.belcobtm.domain.wallet.item

class CoinFeeDataItem(
    val scale: Int,
    val txFee: Double,
    val byteFee: Long,
    val recallFee: Double?,
    val gasPrice: Double,
    val gasLimit: Double,
    val profitExchange: Double,
    val walletAddress: String, //server address
    val contractAddress: String
)