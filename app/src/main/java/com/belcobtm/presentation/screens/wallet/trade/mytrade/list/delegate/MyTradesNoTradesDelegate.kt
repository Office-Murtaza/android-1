package com.belcobtm.presentation.screens.wallet.trade.mytrade.list.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemMyTradesNoTradesBinding
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.screens.wallet.trade.mytrade.list.model.NoTradesCreatedItem

class MyTradesNoTradesDelegate(
    private val createNewTradeListener: () -> Unit
) : AdapterDelegate<NoTradesCreatedItem, MyTradesNoTradesViewHolder>() {
    override val viewType: Int
        get() = NoTradesCreatedItem.NO_TRADES_ITEM_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): MyTradesNoTradesViewHolder =
        MyTradesNoTradesViewHolder(
            ItemMyTradesNoTradesBinding.inflate(inflater, parent, false),
            createNewTradeListener
        )
}

class MyTradesNoTradesViewHolder(
    binding: ItemMyTradesNoTradesBinding,
    createNewTradeListener: () -> Unit
) : MultiTypeViewHolder<NoTradesCreatedItem>(binding.root) {

    init {
        binding.openCreateTradeButton.setOnClickListener {
            createNewTradeListener()
        }
    }

    override fun bind(model: NoTradesCreatedItem) {
        // no-op
    }

}