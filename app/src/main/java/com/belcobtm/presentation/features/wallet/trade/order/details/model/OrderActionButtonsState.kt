package com.belcobtm.presentation.features.wallet.trade.order.details.model

import androidx.annotation.StringRes
import com.belcobtm.data.model.trade.OrderStatus

data class OrderActionButtonsState(
    @StringRes val primaryButtonTitleRes: Int = 0,
    @StringRes val secondaryButtonTitleRes: Int = 0,
    val showPrimaryButton: Boolean = false,
    val showSecondaryButton: Boolean = false,
    @OrderStatus val primaryStatusId: Int = OrderStatus.UNDEFINED,
    @OrderStatus val secondaryStatusId: Int = OrderStatus.UNDEFINED
)