package com.belcobtm.presentation.features.wallet.balance.adapter

data class CoinListItem(
    val code: String,
    val balanceCrypto: Double,
    val balanceFiat: Double,
    val priceUsd: Double
)