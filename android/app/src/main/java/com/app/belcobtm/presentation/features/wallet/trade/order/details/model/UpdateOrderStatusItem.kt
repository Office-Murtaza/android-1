package com.app.belcobtm.presentation.features.wallet.trade.order.details.model

import com.app.belcobtm.data.model.trade.OrderStatus

data class UpdateOrderStatusItem(
    val orderId: String,
    @OrderStatus val newStatus: Int
)