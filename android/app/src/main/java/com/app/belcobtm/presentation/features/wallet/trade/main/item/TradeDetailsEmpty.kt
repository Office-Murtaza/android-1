package com.app.belcobtm.presentation.features.wallet.trade.main.item

import com.app.belcobtm.presentation.core.adapter.model.ListItem

class TradeDetailsEmpty : ListItem {

    companion object {
        const val EMPTY_ID = "empty"
        const val TRADE_DETAILS_EMPTY_TYPE = 827
    }

    override val id: String
        get() = EMPTY_ID

    override val type: Int
        get() = TRADE_DETAILS_EMPTY_TYPE

}