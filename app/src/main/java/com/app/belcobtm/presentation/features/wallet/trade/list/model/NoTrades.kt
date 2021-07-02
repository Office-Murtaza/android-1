package com.app.belcobtm.presentation.features.wallet.trade.list.model

import com.app.belcobtm.presentation.core.adapter.model.ListItem

class NoTrades : ListItem {

    companion object {
        const val NO_TRADES_LIST_TYPE = 9
        const val NO_TRADES_ID = "no_trades_id"
    }

    override val id: String
        get() = NO_TRADES_ID

    override val type: Int
        get() = NO_TRADES_LIST_TYPE
}