package com.belcobtm.domain.trade.list.filter.mapper

import com.belcobtm.data.model.trade.filter.TradeFilter
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem

class TradeFilterMapper {

    fun map(tradeFilterItem: TradeFilterItem): TradeFilter =
        with(tradeFilterItem) {
            val selectedOptions = paymentOptions.asSequence()
                .filter(AvailableTradePaymentOption::selected)
                .map { it.payment.paymentId }
                .toList()
            TradeFilter(
                (coins.find { it.selected } ?: coins.first()).coinCode, selectedOptions,
                distanceFilterEnabled, minDistance, maxDistance, sortOption
            )
        }
}
