package com.app.belcobtm.presentation.features.wallet.trade.main.adapter.holder

import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemTradeListBinding
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem

class TradeDetailsItemViewHolder(
    private val binding: ItemTradeListBinding,
    listener: (TradeDetailsItem) -> Unit
) : MultiTypeViewHolder<TradeDetailsItem>(binding.root) {

    init {
        binding.root.setOnClickListener {
            listener.invoke(model)
        }
    }

    override fun bind(model: TradeDetailsItem) {
        with(binding) {
            userNameView.text =
                root.context.getString(
                    R.string.trade_screen_user_field, model.userName,
                    model.tradeCount, model.rate.toString(), model.distance
                )
            paymentMethodView.text = model.paymentMethod
            priceView.text = root.context.getString(R.string.text_usd, model.price.toStringUsd())
            priceLimitView.text = root.context.getString(
                R.string.text_usd, "${model.minLimit} - ${model.maxLimit}"
            )
        }
    }
}