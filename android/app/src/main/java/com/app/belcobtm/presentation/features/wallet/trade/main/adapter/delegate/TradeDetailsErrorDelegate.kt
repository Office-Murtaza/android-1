package com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemErrorBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.holder.TradeDetailsErrorViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsError

class TradeDetailsErrorDelegate : AdapterDelegate<TradeDetailsError, TradeDetailsErrorViewHolder>() {

    override val viewType: Int
        get() = TradeDetailsError.TRADE_DETAILS_ERROR_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeDetailsErrorViewHolder =
        TradeDetailsErrorViewHolder(ItemErrorBinding.inflate(inflater, parent, false))
}