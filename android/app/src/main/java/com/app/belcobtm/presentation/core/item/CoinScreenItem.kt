package com.app.belcobtm.presentation.core.item

import com.app.belcobtm.domain.wallet.item.CoinDataItem

data class CoinScreenItem(
    val balanceCoin: Double,
    val balanceUsd: Double,
    val priceUsd: Double,
    val reservedBalanceCoin: Double,
    val reservedBalanceUsd: Double,
    val code: String,
    val publicKey: String
)

fun CoinDataItem.mapToScreenItem(): CoinScreenItem = CoinScreenItem(
    balanceCoin = balanceCoin,
    balanceUsd = balanceUsd,
    priceUsd = priceUsd,
    reservedBalanceCoin = reservedBalanceCoin,
    reservedBalanceUsd = reservedBalanceUsd,
    code = code,
    publicKey = publicKey
)