package com.belcobtm.presentation.features.wallet.trade.order.details.model

import com.belcobtm.data.model.trade.OrderStatus

data class UpdateOrderStatusItem(
    val orderId: String,
    @OrderStatus val newStatus: Int
)