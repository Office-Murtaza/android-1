package com.belcobtm.domain.wallet.item

import com.belcobtm.domain.wallet.LocalCoinType

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
    data class Details(val walletAddress: String)
}

fun CoinDataItem.isEthRelatedCoin(): Boolean {
    return this.code.isEthRelatedCoinCode()
}

fun CoinDataItem.isBtcCoin(): Boolean {
    return code == LocalCoinType.BTC.name ||
            code == LocalCoinType.BCH.name ||
            code == LocalCoinType.LTC.name ||
            code == LocalCoinType.DASH.name ||
            code == LocalCoinType.DOGE.name
}

fun String.isEthRelatedCoinCode(): Boolean {
    return this == LocalCoinType.USDC.name || this == LocalCoinType.CATM.name
}