package com.app.belcobtm.presentation.features.wallet.trade.edit

import com.app.belcobtm.data.model.trade.PaymentOption

data class EditTradeItem(
    val tradeId: Int,
    val price: Double,
    val minAmount: Int,
    val maxAmount: Int,
    val terms: String,
    val paymentOptions: List<@PaymentOption Int>
)