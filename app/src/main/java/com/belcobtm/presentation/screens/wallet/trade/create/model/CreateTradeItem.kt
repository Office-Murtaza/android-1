package com.belcobtm.presentation.screens.wallet.trade.create.model

import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.data.model.trade.TradeType

data class CreateTradeItem(
    @TradeType val tradeType: Int,
    val coinCode: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val terms: String,
    val feePercent: Double,
    val fiatAmount: Double,
    val paymentOptions: List<@PaymentOption Int>
)