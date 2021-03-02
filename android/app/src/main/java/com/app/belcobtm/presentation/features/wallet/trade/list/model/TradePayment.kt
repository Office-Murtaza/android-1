package com.app.belcobtm.presentation.features.wallet.trade.list.model

import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class TradePayment(
    @PaymentOption val paymentId: Int,
    val icon: Int
) : ListItem {

    companion object {
        const val TRADE_PAYMENT_LIST_TYPE = 2
    }

    override val id: String
        get() = paymentId.toString()

    override val type: Int
        get() = TRADE_PAYMENT_LIST_TYPE

}