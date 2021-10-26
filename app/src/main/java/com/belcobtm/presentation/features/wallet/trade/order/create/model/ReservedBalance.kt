package com.belcobtm.presentation.features.wallet.trade.order.create.model

data class ReservedBalance(
    val reservedBalanceCrypto: Double,
    val reservedBalanceUsd: String,
    val coinName: String
)