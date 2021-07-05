package com.belcobtm.presentation.features.wallet.trade.list.filter.model

import com.belcobtm.data.model.trade.filter.SortOption
import com.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

data class TradeFilterItem(
    val coins: List<CoinCodeListItem>,
    val paymentOptions: List<AvailableTradePaymentOption>,
    val distanceFilterEnabled: Boolean,
    val minDistance: Int,
    val maxDistance: Int,
    @SortOption val sortOption: Int
)