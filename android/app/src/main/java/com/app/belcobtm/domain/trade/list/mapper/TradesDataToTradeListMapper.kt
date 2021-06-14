package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.inmemory.trade.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.data.model.trade.filter.SortOption
import com.app.belcobtm.data.model.trade.filter.TradeFilter
import com.app.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradesDataToTradeListMapper(
    private val tradeMapper: TradeToTradeItemMapper
) {

    fun map(
        tradeData: TradeData,
        params: ObserveTradesUseCase.Params,
        filter: TradeFilter?,
        userId: String
    ): List<TradeItem> =
        tradeData.trades
            .values
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
            .sortedWith(Comparator { t, t2 ->
                when {
                    filter?.sortOption == SortOption.DISTANCE ->
                        t.distance.compareTo(t2.distance)
                    params.tradeType == TradeType.BUY ->
                        t.price.compareTo(t2.price)
                    else ->
                        t2.price.compareTo(t.price)
                }
            })
            .toList()
}