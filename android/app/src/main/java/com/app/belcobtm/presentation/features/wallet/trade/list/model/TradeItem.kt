package com.app.belcobtm.presentation.features.wallet.trade.list.model

import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.adapter.model.ListItem

data class TradeItem(
    val tradeId: Int,
    val tradeType: Int,
    val coin: LocalCoinType,
    val status: Int,
    val createDate: String,
    val price: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val paymentMethods: List<TradePayment>,
    val terms: String,
    val makerId: Int,
    val makerPublicId: String,
    val makerTotalTrades: Int,
    val makerTradingRate: Double,
    val distance: Double
) : ListItem {

    companion object {
        const val TRADE_ITEM_LIST_TYPE = 1
    }

    override val id: String
        get() = tradeId.toString()

    override val type: Int
        get() = TRADE_ITEM_LIST_TYPE

}