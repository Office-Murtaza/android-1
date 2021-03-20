package com.app.belcobtm.domain.wallet.item

class CoinDetailsDataItem(
    val txFee: Double,
    val byteFee: Long,
    val scale: Int,
    val recallFee: Double?,
    val gasPrice: Double,
    val gasLimit: Double,
    val profitExchange: Double,
    val swapProfitPercent: Double,
    val platformTradeFee: Double,
    val walletAddress: String, //server address
    val contractAddress: String,
    val convertedTxFee: Double
)