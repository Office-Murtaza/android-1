package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.presentation.core.adapter.model.ListItem

class TradeDetailsError : ListItem {

    companion object {
        const val ERROR_ID = "error"
        const val TRADE_DETAILS_ERROR_TYPE = 829
    }

    override val id: String
        get() = ERROR_ID

    override val type: Int
        get() = TRADE_DETAILS_ERROR_TYPE
}