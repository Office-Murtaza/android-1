package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.inmemory.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradesDataToTradeListMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(
        tradeData: TradeData,
        params: ObserveTradesUseCase.Params,
        filter: TradeFilter?,
        userId: Int
    ): List<TradeItem> =
        tradeData.trades
            .asSequence()
            .filter { it.type == params.tradeType }
            .filter { it.makerId != userId }
            .let { sequence ->
                filter?.let { filter ->
                    sequence.filter { it.coinCode == filter.coinCode }
                        .filter { trade ->
                            trade.paymentMethods.any { tradePaymentOption ->
                                filter.paymentOptions.any { it == tradePaymentOption }
                            }
                        }
                        .filter {
                            !filter.filterByDistanceEnalbed || it.distance == UNDEFINED_DISTANCE ||
                                    (it.distance in filter.minDistance.toDouble()..filter.maxDistance.toDouble())
                        }
                } ?: sequence
            }
            .take(params.numbersToLoad)
            .map(tradeMapper::map)
            // TODO add sort
            .toList()
}