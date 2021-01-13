package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.presentation.core.adapter.model.ListItem

interface TradeDetailsItem : ListItem {
    val tradeId: Int
    val minLimit: Int
    val maxLimit: Int
    val tradeCount: Int
    val distance: Int
    val rate: Double
    val userName: String
    val paymentMethod: String
    val price: Double
    val isBuyType: Boolean

    companion object {
        const val TRADE_DETAILS_ITEM_TYPE = 830
    }
}