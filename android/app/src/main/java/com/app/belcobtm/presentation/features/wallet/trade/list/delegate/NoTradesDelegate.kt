package com.app.belcobtm.presentation.features.wallet.trade.list.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemNoTradesBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.list.model.NoTrades

class NoTradesDelegate(
    private val onResetFilterClickListener: () -> Unit
) : AdapterDelegate<NoTrades, NoTradesViewHolder>() {

    override val viewType: Int
        get() = NoTrades.NO_TRADES_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): NoTradesViewHolder =
        NoTradesViewHolder(
            ItemNoTradesBinding.inflate(inflater, parent, false),
            onResetFilterClickListener
        )
}

class NoTradesViewHolder(
    binding: ItemNoTradesBinding,
    onResetFilterClickListener: () -> Unit
) : MultiTypeViewHolder<NoTrades>(binding.root) {

    init {
        binding.resetFilters.setOnClickListener {
            onResetFilterClickListener()
        }
    }

    override fun bind(model: NoTrades) {
        // no-op
    }
}