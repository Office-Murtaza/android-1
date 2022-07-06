package com.belcobtm.domain.trade.model.filter

import com.belcobtm.domain.trade.model.PaymentMethodType

data class TradeFilter(
    val coinCode: String,
    val paymentOptions: List<PaymentMethodType>,
    val filterByDistanceEnabled: Boolean,
    val minDistance: Int,
    val maxDistance: Int,
    @SortOption val sortOption: Int
)
