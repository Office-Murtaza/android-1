package com.belcobtm.presentation.screens.wallet.trade.edit

import com.belcobtm.domain.trade.model.PaymentMethodType

data class EditTradeItem(
    val tradeId: String,
    val price: Double,
    val minAmount: Int,
    val maxAmount: Int,
    val terms: String,
    val feePercent: Double,
    val fiatAmount: Double,
    val paymentOptions: List<PaymentMethodType>
)
