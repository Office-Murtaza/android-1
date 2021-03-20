package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem

data class GetCoinDetailsResponse(
    val txFee: Double,
    val byteFee: Long?,
    val scale: Int?,
    val recallFee: Double?,
    val gasPrice: Double?,
    val gasLimit: Double?,
    val profitExchange: Double,
    val swapProfitPercent: Double,
    val platformTradeFee: Double,
    val walletAddress: String?,
    val contractAddress: String?,
    val convertedTxFee: Double?,
)

fun GetCoinDetailsResponse.mapToDataItem(): CoinDetailsDataItem = CoinDetailsDataItem(
    txFee = txFee,
    profitExchange = profitExchange,
    swapProfitPercent = swapProfitPercent,
    byteFee = byteFee ?: 0,
    scale = scale ?: 0,
    recallFee = recallFee,
    gasPrice = gasPrice ?: 0.0,
    gasLimit = gasLimit ?: 0.0,
    walletAddress = walletAddress ?: "",
    contractAddress = contractAddress ?: "",
    convertedTxFee = convertedTxFee ?: 0.0,
    platformTradeFee = platformTradeFee
)