package com.app.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import com.app.belcobtm.data.model.trade.TraderStatus
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class OrderItem(
    val orderId: String,
    val trade: TradeItem,
    val myTradeId: String,
    val mappedTradeType: Int,
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
    @TraderStatus val makerStatusId: Int,
    @DrawableRes val makerStatusIconRes: Int,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTotalTradesFormatted: String,
    val makerTradingRate: Double?,
    val takerId: String,
    @TraderStatus val takerStatusId: Int,
    @DrawableRes val takerStatusIconRes: Int,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTotalTradesFormatted: String,
    val takerTradingRate: Double?
) : ListItem {

    companion object {
        const val OPEN_ORDER_TYPE = 4
    }

    override val id: String
        get() = orderId.toString()

    override val type: Int
        get() = OPEN_ORDER_TYPE
}