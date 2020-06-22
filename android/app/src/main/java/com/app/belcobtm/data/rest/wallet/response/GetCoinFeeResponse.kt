package com.app.belcobtm.data.rest.wallet.response

import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem

data class GetCoinFeeResponse(
    val txFee: Double,
    val byteFee: Double?,
    val gasPrice: Double?,
    val gasLimit: Double?,
    val profitC2C: Double,
    val walletAddress: String?,
    val contractAddress: String?
)

fun GetCoinFeeResponse.mapToDataItem(): CoinFeeDataItem = CoinFeeDataItem(
    txFee = txFee,
    profitC2C = profitC2C,
    byteFee = byteFee ?: 0.0,
    gasPrice = gasPrice ?: 0.0,
    gasLimit = gasLimit ?: 0.0,
    walletAddress = walletAddress ?: "",
    contractAddress = contractAddress ?: ""
)