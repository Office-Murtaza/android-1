package com.app.belcobtm.domain.trade.list.mapper

import com.app.belcobtm.data.model.trade.TradeData
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class TradesDataToMyTradeMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(tradeData: TradeData, userId: Int): List<ListItem> =
        tradeData.trades
            .asSequence()
            .filter { it.makerId == userId }
            .map(tradeMapper::map)
            .sortedWith(Comparator { t, t2 -> t2.timestamp.compareTo(t.timestamp) })
            .toList()
            .ifEmpty { listOf(NoTradesCreatedItem()) }
}