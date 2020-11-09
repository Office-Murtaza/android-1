package com.app.belcobtm.presentation.features.wallet.balance.adapter

sealed class BalanceListItem {
    data class Coin(
        val code: String,
        val balanceCrypto: Double,
        val balanceFiat: Double,
        val priceUsd: Double
    ) : BalanceListItem()
}