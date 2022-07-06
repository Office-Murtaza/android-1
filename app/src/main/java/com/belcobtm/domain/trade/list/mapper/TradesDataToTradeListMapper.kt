package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.list.ObserveTradesUseCase
import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.filter.SortOption
import com.belcobtm.domain.trade.model.filter.TradeFilter
import com.belcobtm.domain.trade.model.trade.TradeDomainModel.Companion.UNDEFINED_DISTANCE
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem

class TradesDataToTradeListMapper(
    private val tradeMapper: TradeToTradeItemMapper
) {

    fun map(
        tradeData: TradeHistoryDomainModel,
        params: ObserveTradesUseCase.Params,
        filter: TradeFilter?,
        userId: String
    ): List<TradeItem> =
        tradeData.trades
            .values
            .asSequence()
            .filter { it.type == params.tradeType }
            .filter { it.status != TradeStatus.DELETED }
            .filter { it.makerId != userId }
            .let { sequence ->
                filter?.let { filter ->
                    sequence.filter { it.coin.name == filter.coinCode }
                        .filter { trade ->
                            trade.paymentMethods.any { tradePaymentOption ->
                                filter.paymentOptions.any { it == tradePaymentOption }
                            }
                        }
                        .filter {
                            !filter.filterByDistanceEnabled || it.distance == UNDEFINED_DISTANCE ||
                                (it.distance in filter.minDistance.toDouble()..filter.maxDistance.toDouble())
                        }
                } ?: sequence
            }
            .take(params.numbersToLoad)
            .map(tradeMapper::map)
            .sortedWith { t, t2 ->
                when {
                    filter?.sortOption == SortOption.DISTANCE ->
                        t.distance.compareTo(t2.distance)
                    params.tradeType == TradeType.BUY ->
                        t.price.compareTo(t2.price)
                    else ->
                        t2.price.compareTo(t.price)
                }
            }
            .toList()

}
