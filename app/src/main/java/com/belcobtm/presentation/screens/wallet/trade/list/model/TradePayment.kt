package com.belcobtm.presentation.screens.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.domain.trade.model.PaymentMethodType
import com.belcobtm.presentation.core.adapter.model.ListItem

data class TradePayment(
    val paymentId: PaymentMethodType,
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