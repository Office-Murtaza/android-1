package com.belcobtm.presentation.features.wallet.trade.mytrade.list.model

import com.belcobtm.presentation.core.adapter.model.ListItem

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