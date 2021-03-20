package com.app.belcobtm.presentation.features.wallet.trade.buysell.model

data class TradeFee(
    val platformFeePercent: Double,
    val platformFeeCrypto: Double,
    val coinCode: String
)