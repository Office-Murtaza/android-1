package com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemEmptyBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.holder.TradeDetailsEmptyViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsEmpty

class TradeDetailsEmptyDelegate : AdapterDelegate<TradeDetailsEmpty, TradeDetailsEmptyViewHolder>() {

    override val viewType: Int
        get() = TradeDetailsEmpty.TRADE_DETAILS_EMPTY_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeDetailsEmptyViewHolder =
        TradeDetailsEmptyViewHolder(ItemEmptyBinding.inflate(inflater, parent, false))
}