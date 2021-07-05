package com.belcobtm.data.rest.trade.request

import com.belcobtm.data.model.trade.OrderStatus

data class UpdateOrderRequest(
    val id: String,
    @OrderStatus val status: Int?,
    val rate: Int? = null
)