package com.belcobtm.presentation.screens.wallet.trade.order.create.model

import com.belcobtm.domain.wallet.LocalCoinType

data class TradeOrderItem(
    val tradeId: String,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val feePercent: Double,
    val coin: LocalCoinType
)
