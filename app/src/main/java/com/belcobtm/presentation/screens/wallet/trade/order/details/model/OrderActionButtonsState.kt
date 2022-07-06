package com.belcobtm.presentation.screens.wallet.trade.order.details.model

import androidx.annotation.StringRes
import com.belcobtm.domain.trade.model.order.OrderStatus

data class OrderActionButtonsState(
    @StringRes val primaryButtonTitleRes: Int = 0,
    @StringRes val secondaryButtonTitleRes: Int = 0,
    val showPrimaryButton: Boolean = false,
    val showSecondaryButton: Boolean = false,
    val primaryStatus: OrderStatus = OrderStatus.UNKNOWN,
    val secondaryStatus: OrderStatus = OrderStatus.UNKNOWN
)