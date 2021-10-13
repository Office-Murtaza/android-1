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
    data class Details(val index: Int, val walletAddress: String)
}

fun CoinDataItem.isEthRelatedCoin(): Boolean = code.isEthRelatedCoinCode()

fun CoinDataItem.isBtcCoin(): Boolean = code.isBtcCoin()

fun String.isBtcCoin(): Boolean {
    return this == LocalCoinType.BTC.name ||
            this == LocalCoinType.BCH.name ||
            this == LocalCoinType.LTC.name ||
            this == LocalCoinType.DASH.name ||
            this == LocalCoinType.DOGE.name
}

fun String.isEthRelatedCoinCode(): Boolean {
    return this == LocalCoinType.USDC.name || this == LocalCoinType.CATM.name
}