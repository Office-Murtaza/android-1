package com.belcobtm.presentation.features.wallet.trade.edit

import com.belcobtm.data.model.trade.PaymentOption

data class EditTradeItem(
    val tradeId: String,
    val price: Double,
    val minAmount: Int,
    val maxAmount: Int,
    val terms: String,
    val paymentOptions: List<@PaymentOption Int>
)