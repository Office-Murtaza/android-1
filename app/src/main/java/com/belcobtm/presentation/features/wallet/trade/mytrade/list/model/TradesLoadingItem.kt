package com.belcobtm.presentation.features.wallet.trade.mytrade.list.model

import com.belcobtm.presentation.core.adapter.model.ListItem

data class TradesLoadingItem(
    override val id: String = TRADES_LOADING_ITEM_ID
) : ListItem {

    companion object {
        const val TRADES_LOADING_ITEM_ID = "trades_loading_item"
        const val TRADES_LOADING_ITEM_ITEM_TYPE = 3
    }

    override val type: Int
        get() = TRADES_LOADING_ITEM_ITEM_TYPE
}