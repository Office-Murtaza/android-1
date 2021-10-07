package com.belcobtm.data.rest.trade.request

import com.belcobtm.data.model.trade.TradeStatus

data class CancelTradeRequest(val id: String, @TradeStatus val status: Int)