package com.belcobtm.presentation.screens.wallet.trade.order.create.model

data class TradeFee(
    val platformFeePercent: Double,
    val platformFeeCrypto: Double,
    val coinCode: String
)