package com.app.belcobtm.presentation.features.wallet.trade.list.delegate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.data.inmemory.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.databinding.ItemTradeBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class TradeItemDelegate(
    private val onTradeClickListener: (TradeItem) -> Unit
) : AdapterDelegate<TradeItem, TradeItemViewHolder>() {
    override val viewType: Int
        get() = TradeItem.TRADE_ITEM_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeItemViewHolder =
        TradeItemViewHolder(
            ItemTradeBinding.inflate(inflater, parent, false),
            onTradeClickListener
        )
}

class TradeItemViewHolder(
    private val binding: ItemTradeBinding,
    onTradeClickListener: (TradeItem) -> Unit
) : MultiTypeViewHolder<TradeItem>(binding.root) {

    private val paymentAdapter = MultiTypeAdapter()

    init {
        paymentAdapter.registerDelegate(TradePaymentOptionDelegate())
        binding.paymentOptions.adapter = paymentAdapter
        binding.root.setOnClickListener {
            onTradeClickListener(model)
        }
    }

    override fun bind(model: TradeItem) {
        with(model) {
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.makerPublicId.text = makerPublicId
            binding.makerPublicId.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_account_circle, 0, makerStatusIcon, 0
            )
            binding.priceRange.text = binding.root.context.getString(
                R.string.trade_list_item_price_range_format,
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, minLimit.toStringUsd()),
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, maxLimit.toStringUsd())
            )
            binding.priceLabel.text = binding.root.context.getString(
                R.string.trade_list_item_usd_formatted, price.toStringUsd()
            )
            binding.makerTradeCountLabel.text = binding.root.resources
                .getString(R.string.trade_list_item_total_trades_formatted, makerTotalTrades)
            binding.makerRateLabel.text = makerTradingRate.toString()
            paymentAdapter.update(paymentMethods)
            if (distance == UNDEFINED_DISTANCE) {
                binding.distanceLabel.visibility = View.GONE
            } else {
                binding.distanceLabel.visibility = View.VISIBLE
                binding.distanceLabel.text = distanceFormatted
            }
        }
    }

}