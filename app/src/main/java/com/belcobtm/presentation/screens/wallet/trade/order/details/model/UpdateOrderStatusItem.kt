package com.belcobtm.presentation.screens.wallet.trade.order.details.model

import com.belcobtm.domain.trade.model.order.OrderStatus

data class UpdateOrderStatusItem(
    val orderId: String,
    val newStatus: OrderStatus
)
