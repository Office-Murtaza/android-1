package com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemProgressBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.holder.TradeDetailsLoadingViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsLoading

class TradeDetailsLoadingDelegate : AdapterDelegate<TradeDetailsLoading, TradeDetailsLoadingViewHolder>() {

    override val viewType: Int
        get() = TradeDetailsLoading.TRADE_DETAILS_LOADING_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeDetailsLoadingViewHolder =
        TradeDetailsLoadingViewHolder(ItemProgressBinding.inflate(inflater, parent, false))
}