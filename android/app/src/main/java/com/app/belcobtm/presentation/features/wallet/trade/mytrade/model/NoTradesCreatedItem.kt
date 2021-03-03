package com.app.belcobtm.presentation.features.wallet.trade.mytrade.model

import com.app.belcobtm.presentation.core.adapter.model.ListItem

class NoTradesCreatedItem : ListItem {

    companion object {
        const val NO_TRADES_ITEM_ID = "no_trades_id"
        const val NO_TRADES_ITEM_TYPE = 2
    }

    override val id: String
        get() = NO_TRADES_ITEM_ID

    override val type: Int
        get() = NO_TRADES_ITEM_TYPE
}