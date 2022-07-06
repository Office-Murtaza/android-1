package com.belcobtm.domain.trade.model

import com.belcobtm.domain.trade.model.order.OrderDomainModel
import com.belcobtm.domain.trade.model.trade.TradeDomainModel

data class TradeHistoryDomainModel(
    val trades: MutableMap<String, TradeDomainModel>,
    val orders: MutableMap<String, OrderDomainModel>,
    val statistics: UserTradeStatsDomainModel
)
