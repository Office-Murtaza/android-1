package com.belcobtm.presentation.screens.wallet.trade.create.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.R
import com.belcobtm.databinding.ItemTradePaymentOptionBinding
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment

class TradePaymentOptionDelegate(
    private val paymentOptionClickListener: (AvailableTradePaymentOption) -> Unit
) : AdapterDelegate<AvailableTradePaymentOption, TradePaymentOptionViewHolder>() {

    override val viewType: Int
        get() = TradePayment.TRADE_PAYMENT_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradePaymentOptionViewHolder =
        TradePaymentOptionViewHolder(
            paymentOptionClickListener,
            ItemTradePaymentOptionBinding.inflate(inflater, parent, false)
        )
}

class TradePaymentOptionViewHolder(
    private val paymentOptionClickListener: (AvailableTradePaymentOption) -> Unit,
    private val binding: ItemTradePaymentOptionBinding
) : MultiTypeViewHolder<AvailableTradePaymentOption>(binding.root) {

    override fun bind(model: AvailableTradePaymentOption) {
        binding.paymentOption.setOnCheckedChangeListener(null)
        binding.paymentOption.setChipIconResource(model.payment.icon)
        binding.paymentOption.setText(model.payment.title)
        binding.paymentOption.isChecked = model.selected
        updateStroke(model.selected)
        binding.paymentOption.setOnCheckedChangeListener { _, isChecked ->
            paymentOptionClickListener(model)
            updateStroke(isChecked)
        }
    }

    private fun updateStroke(isChecked: Boolean) {
        binding.paymentOption.chipStrokeWidth = if (isChecked) {
            binding.root.resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
        } else {
            0.0f
        }
    }
}