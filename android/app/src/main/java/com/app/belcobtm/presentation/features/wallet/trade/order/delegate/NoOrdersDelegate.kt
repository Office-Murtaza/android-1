package com.app.belcobtm.presentation.features.wallet.trade.order.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemNoOpenOrdersBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.list.model.NoOrders

class NoOrdersDelegate : AdapterDelegate<NoOrders, NoOrdersViewHolder>() {

    override val viewType: Int
        get() = NoOrders.NO_ORDERS_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): NoOrdersViewHolder =
        NoOrdersViewHolder(ItemNoOpenOrdersBinding.inflate(inflater, parent, false))
}

class NoOrdersViewHolder(
    binding: ItemNoOpenOrdersBinding
) : MultiTypeViewHolder<NoOrders>(binding.root) {

    override fun bind(model: NoOrders) {
        // no-op
    }
}