package com.belcobtm.presentation.features.wallet.trade.list.model

import com.belcobtm.presentation.core.adapter.model.ListItem

class NoOrders : ListItem {

    companion object {
        const val NO_ORDERS_LIST_TYPE = 12
        const val NO_ORDERS_ID = "no_orders_id"
    }

    override val id: String
        get() = NO_ORDERS_ID

    override val type: Int
        get() = NO_ORDERS_LIST_TYPE
}