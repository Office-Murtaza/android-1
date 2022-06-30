package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.data.model.trade.TradeStatus
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class TradesDataToMyTradeMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(tradeData: TradeData, userId: String): List<ListItem> =
        tradeData.trades
            .values
            .asSequence()
            .filter { it.makerId == userId }
            .filter { it.status != TradeStatus.DELETED }
            .map(tradeMapper::map)
            .sortedWith { t, t2 -> t2.timestamp.compareTo(t.timestamp) }
            .toList()
            .ifEmpty { listOf(NoTradesCreatedItem()) }
}