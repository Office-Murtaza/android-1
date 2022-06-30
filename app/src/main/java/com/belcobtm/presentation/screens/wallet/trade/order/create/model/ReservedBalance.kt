package com.belcobtm.presentation.screens.wallet.trade.order.create.model

data class ReservedBalance(
    val reservedBalanceCrypto: Double,
    val reservedBalanceUsd: String,
    val coinName: String
)