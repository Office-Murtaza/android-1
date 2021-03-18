package com.app.belcobtm.data.model.trade.filter

import com.app.belcobtm.data.model.trade.PaymentOption

data class TradeFilter(
    val coinCode: String,
    val paymentOptions: List<@PaymentOption Int>,
    val filterByDistanceEnalbed: Boolean,
    val minDistance: Int,
    val maxDistance: Int,
    @SortOption val sortOption: Int
)