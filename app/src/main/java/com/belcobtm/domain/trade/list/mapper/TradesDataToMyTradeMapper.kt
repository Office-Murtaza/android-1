package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem

class TradesDataToMyTradeMapper(private val tradeMapper: TradeToTradeItemMapper) {

    fun map(tradeData: TradeHistoryDomainModel, userId: String): List<TradeItem> =
        tradeData.trades
            .values
            .asSequence()
            .filter { it.makerId == userId && it.status != TradeStatus.DELETED }
            .map(tradeMapper::map)
            .sortedWith { t, t2 -> t2.timestamp.compareTo(t.timestamp) }
            .toList()

}
