package com.belcobtm.data.rest.trade.request

data class UpdateOrderRequest(
    val id: String,
    val status: String?,
    val rate: Int? = null
)
