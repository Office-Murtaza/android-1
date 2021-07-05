package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.data.model.trade.TradeData
import com.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class TradesDataToOrderListMapper(private val orderMapper: TradeOrderDataToItemMapper) {

    fun map(tradeData: TradeData, userId: String): List<OrderItem> =
        tradeData.orders
            .values
            .asSequence()
            .filter { it.makerId == userId || it.takerId == userId }
            .mapNotNull { orderMapper.map(it, tradeData, userId) }
            .sortedWith { t, t2 -> t2.timestamp.compareTo(t.timestamp) }
            .toList()
}