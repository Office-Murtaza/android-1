package com.app.belcobtm.domain.trade.list.filter.mapper

import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem

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
