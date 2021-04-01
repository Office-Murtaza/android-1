package com.app.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.belcobtm.data.model.trade.OrderStatus

data class OrderStatusItem(
    @OrderStatus val statusId: Int,
    @StringRes val statusLabelId: Int,
    @DrawableRes val statusDrawableId: Int
)