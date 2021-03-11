package com.app.belcobtm.presentation.features.wallet.trade.create.model

import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.TradeType

data class CreateTradeItem(
    @TradeType val tradeType: Int,
    val coinCode: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val terms: String,
    val paymentOptions: List<@PaymentOption Int>
)