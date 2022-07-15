package com.belcobtm.domain.trade.list.mapper

import com.belcobtm.domain.trade.model.TradeHistoryDomainModel
import com.belcobtm.domain.trade.model.order.OrderStatus
import com.belcobtm.presentation.screens.wallet.trade.list.model.OrderItem

class TradesDataToOrderListMapper(private val orderMapper: TradeOrderDataToItemMapper) {

    fun map(tradeData: TradeHistoryDomainModel, userId: String): List<OrderItem> =
        tradeData.orders
            .values
            .asSequence()
            .filter { (it.makerUserId == userId || it.takerUserId == userId) && it.status != OrderStatus.DELETED }
            .mapNotNull { orderMapper.map(it, tradeData, userId) }
            .sortedWith { t, t2 -> t2.timestamp.compareTo(t.timestamp) }
            .toList()

}
