package com.app.belcobtm.presentation.features.wallet.trade.mytrade.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemMyTradesNoTradesBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.mytrade.model.NoTradesCreatedItem

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