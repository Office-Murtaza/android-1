package com.belcobtm.presentation.screens.wallet.trade.list.model

import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.adapter.model.ListItem

data class OrderItem(
    val orderId: String,
    val trade: TradeItem,
    val myTradeId: String,
    val mappedTradeType: TradeType,
    val coin: LocalCoinType,
    val orderStatus: OrderStatusItem,
    val timestamp: Long,
    val price: Double,
    val priceFormatted: String,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val fiatAmountFormatted: String,
    val paymentOptions: List<TradePayment>,
    val terms: String,
    val makerId: String,
    val makerRate: Int,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTotalTradesFormatted: String,
    val makerTradingRate: Double?,
    val takerId: String,
    val takerRate: Int,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTotalTradesFormatted: String,
    val takerTradingRate: Double?,
    val distanceFormatted: String
) : ListItem {

    companion object {

        const val OPEN_ORDER_TYPE = 4
    }

    override val id: String
        get() = orderId

    override val type: Int
        get() = OPEN_ORDER_TYPE
}