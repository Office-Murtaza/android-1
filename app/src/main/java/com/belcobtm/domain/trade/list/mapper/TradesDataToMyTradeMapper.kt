package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class TradesDataToMyTradeMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(tradeData: TradeHistoryDomainModel, userId: String): List<ListItem> =
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