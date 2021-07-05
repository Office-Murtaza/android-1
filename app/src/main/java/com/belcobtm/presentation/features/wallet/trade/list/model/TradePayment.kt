package com.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.presentation.core.adapter.model.ListItem

data class TradePayment(
    @PaymentOption val paymentId: Int,
    @DrawableRes val icon: Int,
    @StringRes val title: Int
) : ListItem {

    companion object {
        const val TRADE_PAYMENT_LIST_TYPE = 2
    }

    override val id: String
        get() = paymentId.toString()

    override val type: Int
        get() = TRADE_PAYMENT_LIST_TYPE

}