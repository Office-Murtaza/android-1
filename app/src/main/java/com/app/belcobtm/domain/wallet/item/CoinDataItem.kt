package com.app.belcobtm.domain.wallet.item

import com.app.belcobtm.domain.wallet.LocalCoinType

data class CoinDataItem(
    val balanceCoin: Double,
    val balanceUsd: Double,
    val priceUsd: Double,
    val reservedBalanceCoin: Double,
    val reservedBalanceUsd: Double,
    val code: String,
    val publicKey: String,
    val isEnabled: Boolean = true,
    val details: Details
) {
    data class Details(
        val txFee: Double,
        val byteFee: Long,
        val scale: Int,
        val platformSwapFee: Double,
        val platformTradeFee: Double,
        val walletAddress: String,
        val gasLimit: Long?,
        val gasPrice: Long?,
        val convertedTxFee: Double?
    )
}

fun CoinDataItem.isEthRelatedCoin(): Boolean {
    return this.code.isEthRelatedCoinCode()
}

fun String.isEthRelatedCoinCode(): Boolean {
    return this == LocalCoinType.USDC.name || this == LocalCoinType.CATM.name
}