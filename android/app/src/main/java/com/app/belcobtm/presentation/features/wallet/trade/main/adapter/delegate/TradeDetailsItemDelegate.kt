package com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemTradeListBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.holder.TradeDetailsItemViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem

class TradeDetailsItemDelegate(
    private val listener: (TradeDetailsItem) -> Unit
) : AdapterDelegate<TradeDetailsItem, TradeDetailsItemViewHolder>() {

    override val viewType: Int
        get() = TradeDetailsItem.TRADE_DETAILS_ITEM_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeDetailsItemViewHolder =
        TradeDetailsItemViewHolder(ItemTradeListBinding.inflate(inflater, parent, false), listener)
}