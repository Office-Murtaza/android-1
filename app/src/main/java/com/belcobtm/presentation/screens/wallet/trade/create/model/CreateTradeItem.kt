package com.belcobtm.presentation.screens.wallet.trade.create.model

import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.domain.trade.model.trade.TradeType

data class CreateTradeItem(
    val tradeType: TradeType,
    val coinCode: String,
    val price: Int,
    val minLimit: Int,
    val maxLimit: Int,
    val terms: String,
    val feePercent: Double,
    val fiatAmount: Double,
    val paymentOptions: List<PaymentMethodType>
)
