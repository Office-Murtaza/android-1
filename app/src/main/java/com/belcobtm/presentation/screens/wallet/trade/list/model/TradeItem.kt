package com.belcobtm.presentation.screens.wallet.trade.list.model

import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.adapter.model.ListItem

data class TradeItem(
    val tradeId: String,
    val tradeType: TradeType,
    val coin: LocalCoinType,
    val status: TradeStatus,
    val price: Double,
    val timestamp: Long,
    val priceFormatted: String,
    val minLimit: Double,
    val minLimitFormatted: String,
    val maxLimit: Double,
    val maxLimitFormatted: String,
    val ordersCount: Int,
    val paymentMethods: List<TradePayment>,
    val terms: String,
    val makerId: String,
    val makerPublicId: String,
    val makerTotalTrades: Int,
    val makerTotalTradesFormatted: String,
    val makerTradingRate: Double,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val distance: Double,
    val distanceFormatted: String
) : ListItem {

    companion object {

        const val TRADE_ITEM_LIST_TYPE = 1
    }

    override val id: String
        get() = tradeId

    override val type: Int
        get() = TRADE_ITEM_LIST_TYPE

}