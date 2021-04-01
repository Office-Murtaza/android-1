package com.app.belcobtm.presentation.features.wallet.trade.order.create.model

data class TradeFee(
    val platformFeePercent: Double,
    val platformFeeCrypto: Double,
    val coinCode: String
)