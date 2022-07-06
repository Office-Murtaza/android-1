package com.belcobtm.presentation.screens.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.domain.trade.model.order.OrderStatus

data class OrderStatusItem(
    val statusId: OrderStatus,
    @StringRes val statusLabelId: Int,
    @DrawableRes val statusDrawableId: Int
)
