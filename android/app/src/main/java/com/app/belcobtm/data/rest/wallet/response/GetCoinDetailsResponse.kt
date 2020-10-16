package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem

data class GetCoinDetailsResponse(
    val txFee: Double,
    val byteFee: Long?,
    val recallFee: Double?,
    val gasPrice: Double?,
    val gasLimit: Double?,
    val profitExchange: Double,
    val walletAddress: String?,
    val contractAddress: String?
)

fun GetCoinDetailsResponse.mapToDataItem(): CoinDetailsDataItem = CoinDetailsDataItem(
    txFee = txFee,
    profitExchange = profitExchange,
    byteFee = byteFee ?: 0,
    recallFee = recallFee,
    gasPrice = gasPrice ?: 0.0,
    gasLimit = gasLimit ?: 0.0,
    walletAddress = walletAddress ?: "",
    contractAddress = contractAddress ?: ""
)