package com.belcobtm.data.rest.trade.response

import com.belcobtm.data.model.trade.TradeStatus
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.data.model.trade.TraderStatus

data class TradeItemResponse(
    val id: String,
    val coin: String,
    @TradeType val type: Int,
    @TradeStatus val status: Int,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val openOrders: Int,
    val paymentMethods: String,
    val terms: String,
    val makerUserId: String,
    @TraderStatus val makerStatus: Int,
    val makerUsername: String?,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTradeTotal: Int,
    val makerTradeRate: Double,
    val timestamp: Long
)