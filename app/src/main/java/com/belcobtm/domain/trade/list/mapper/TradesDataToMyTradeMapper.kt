package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.features.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class TradesDataToMyTradeMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(tradeData: TradeData, userId: String): List<ListItem> =
        tradeData.trades
            .values
            .asSequence()
            .filter { it.makerId == userId }
            .map(tradeMapper::map)
            .sortedWith(Comparator { t, t2 -> t2.timestamp.compareTo(t.timestamp) })
            .toList()
            .ifEmpty { listOf(NoTradesCreatedItem()) }
}