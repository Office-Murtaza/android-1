package com.app.belcobtm.presentation.features.wallet.trade.create.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemTradePaymentOptionBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class TradePaymentOptionDelegate : AdapterDelegate<AvailableTradePaymentOption, TradePaymentOptionViewHolder>() {

    override val viewType: Int
        get() = TradePayment.TRADE_PAYMENT_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradePaymentOptionViewHolder =
        TradePaymentOptionViewHolder(ItemTradePaymentOptionBinding.inflate(inflater, parent, false))
}

class TradePaymentOptionViewHolder(
    private val binding: ItemTradePaymentOptionBinding
) : MultiTypeViewHolder<AvailableTradePaymentOption>(binding.root) {

    init {
        binding.paymentOption.setOnCheckedChangeListener { _, isChecked ->
            model.selected = isChecked
            binding.paymentOption.chipStrokeWidth = if (isChecked) {
                binding.root.resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
            } else {
                0.0f
            }
        }
    }

    override fun bind(model: AvailableTradePaymentOption) {
        binding.paymentOption.setChipIconResource(model.payment.icon)
        binding.paymentOption.setText(model.payment.title)
        binding.paymentOption.isChecked = model.selected
    }
}