package com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model

import com.belcobtm.presentation.core.adapter.model.ListItem

data class NoTradesCreatedItem(
    override val id: String = NO_TRADES_ITEM_ID
) : ListItem {

    companion object {
        const val NO_TRADES_ITEM_ID = "no_trades_id"
        const val NO_TRADES_ITEM_TYPE = 2
    }

    override val type: Int
        get() = NO_TRADES_ITEM_TYPE
}