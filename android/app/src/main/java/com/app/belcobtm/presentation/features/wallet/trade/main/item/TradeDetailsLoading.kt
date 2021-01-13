package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.presentation.core.adapter.model.ListItem

class TradeDetailsLoading : ListItem {

    companion object {
        const val LOADING_ID = "loading"
        const val TRADE_DETAILS_LOADING_TYPE = 828
    }

    override val id: String
        get() = LOADING_ID

    override val type: Int
        get() = TRADE_DETAILS_LOADING_TYPE
}