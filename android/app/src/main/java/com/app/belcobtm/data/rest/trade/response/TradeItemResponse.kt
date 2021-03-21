package com.app.belcobtm.data.rest.trade.response

import com.app.belcobtm.data.model.trade.TradeStatus
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.data.model.trade.TraderStatus

data class TradeItemResponse(
    val id: Int,
    @TradeType val type: Int,
    val coin: String,
    @TradeStatus val status: Int,
    val timestamp: Long,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val openOrders: Int,
    val paymentMethods: String,
    val terms: String,
    val makerId: Int,
    @TraderStatus val makerStatus: Int,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double
)