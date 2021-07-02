package com.app.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class TradeItem(
    val tradeId: String,
    @TradeType val tradeType: Int,
    val coin: LocalCoinType,
    val status: Int,
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
    @DrawableRes val makerStatusIcon: Int,
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
        get() = tradeId.toString()

    override val type: Int
        get() = TRADE_ITEM_LIST_TYPE

}