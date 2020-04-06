package com.app.belcobtm.api.model.response

import com.app.belcobtm.domain.wallet.CoinFeeDataItem
import java.io.Serializable

data class GetCoinFeeResponse(
    val txFee: Double,
    val byteFee: Double?,
    val gasPrice: Double?,
    val gasLimit: Double?,
    val profitC2C: Double,
    val serverWalletAddress: String
) : Serializable

fun GetCoinFeeResponse.toDataItem(): CoinFeeDataItem = CoinFeeDataItem(
    txFee = txFee,
    profitC2C = profitC2C,
    byteFee = byteFee ?: 0.0,
    gasPrice = gasPrice ?: 0.0,
    gasLimit = gasLimit ?: 0.0,
    serverWalletAddress = serverWalletAddress
)