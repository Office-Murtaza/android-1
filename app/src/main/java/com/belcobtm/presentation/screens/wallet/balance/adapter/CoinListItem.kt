package com.belcobtm.presentation.screens.wallet.balance.adapter

data class CoinListItem(
    val code: String,
    val balanceCrypto: Double,
    val balanceFiat: Double,
    val priceUsd: Double
)