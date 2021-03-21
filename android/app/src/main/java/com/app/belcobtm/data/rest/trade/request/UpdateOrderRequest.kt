package com.app.belcobtm.data.rest.trade.request

import com.app.belcobtm.data.model.trade.OrderStatus

data class UpdateOrderRequest(
    val id: Int,
    @OrderStatus val status: Int
)