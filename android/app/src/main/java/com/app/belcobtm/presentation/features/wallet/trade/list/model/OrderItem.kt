package com.app.belcobtm.presentation.features.wallet.trade.list.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class OrderItem(
    val orderId: Int,
    val tradeId: Int,
    @TradeType val tradeType: Int,
    val coin: LocalCoinType,
    @StringRes val statusLabelId: Int,
    @DrawableRes val statusDrawableId: Int,
    val timestamp: Long,
    val price: Double,
    val cryptoAmount: Double,
    val fiatAmount: Double,
    val paymentOptions: List<TradePayment>,
    val terms: String,
    val makerId: Int,
    val makerPublicId: String,
    val makerLatitude: Double?,
    val makerLongitude: Double?,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val takerId: Int,
    val takerPublicId: String,
    val takerLatitude: Double?,
    val takerLongitude: Double?,
    val takerTotalTrades: Int,
    val takerTradingRate: Double
) : ListItem {

    companion object {
        const val OPEN_ORDER_TYPE = 4
    }

    override val id: String
        get() = orderId.toString()

    override val type: Int
        get() = OPEN_ORDER_TYPE
}