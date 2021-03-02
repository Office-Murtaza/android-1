package com.app.belcobtm.presentation.features.wallet.trade.list.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemPaymentOptionBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class TradePaymentOptionDelegate : AdapterDelegate<TradePayment, TradePaymentOptionViewHolder>() {

    override val viewType: Int
        get() = TradePayment.TRADE_PAYMENT_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradePaymentOptionViewHolder =
        TradePaymentOptionViewHolder(ItemPaymentOptionBinding.inflate(inflater, parent, false))

}

class TradePaymentOptionViewHolder(
    private val binding: ItemPaymentOptionBinding
) : MultiTypeViewHolder<TradePayment>(binding.root) {

    override fun bind(model: TradePayment) {
        binding.root.setImageResource(model.icon)
    }
}