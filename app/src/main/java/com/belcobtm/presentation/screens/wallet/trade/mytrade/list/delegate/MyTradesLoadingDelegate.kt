package com.belcobtm.presentation.screens.wallet.trade.mytrade.list.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemTradesLoadingBinding
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model.TradesLoadingItem

class MyTradesLoadingDelegate : AdapterDelegate<TradesLoadingItem, MyTradesLoadingViewHolder>() {
    override val viewType: Int
        get() = TradesLoadingItem.TRADES_LOADING_ITEM_ITEM_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): MyTradesLoadingViewHolder =
        MyTradesLoadingViewHolder(ItemTradesLoadingBinding.inflate(inflater, parent, false))
}

class MyTradesLoadingViewHolder(
    private val binding: ItemTradesLoadingBinding
) : MultiTypeViewHolder<TradesLoadingItem>(binding.root) {

    override fun bind(model: TradesLoadingItem) {}
}