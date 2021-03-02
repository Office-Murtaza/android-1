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

class TradeItemDelegate : AdapterDelegate<TradeItem, TradeItemViewHolder>() {
    override val viewType: Int
        get() = TradeItem.TRADE_ITEM_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): TradeItemViewHolder =
        TradeItemViewHolder(ItemTradeBinding.inflate(inflater, parent, false))
}

class TradeItemViewHolder(private val binding: ItemTradeBinding) : MultiTypeViewHolder<TradeItem>(binding.root) {

    private val paymentAdapter = MultiTypeAdapter()

    init {
        paymentAdapter.registerDelegate(TradePaymentOptionDelegate())
        binding.paymentOptions.adapter = paymentAdapter
        binding.root.setOnClickListener {
            // TODO open trade details
        }
    }

    override fun bind(model: TradeItem) {
        with(model) {
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.makerPublicId.text = makerPublicId
            binding.priceRange.text = binding.root.context.getString(
                R.string.trade_list_item_price_range_format,
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, minLimit.toStringUsd()),
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, maxLimit.toStringUsd())
            )
            binding.priceLabel.text = binding.root.context.getString(
                R.string.trade_list_item_usd_formatted, price.toStringUsd()
            )
            binding.makerTradeCountLabel.text = makerTotalTrades.toString()
            binding.makerRateLabel.text = makerTradingRate.toString()
            paymentAdapter.update(paymentMethods)
            if (distance == UNDEFINED_DISTANCE) {
                binding.distanceLabel.visibility = View.GONE
            } else {
                binding.distanceLabel.visibility = View.VISIBLE
                // TODO set distance
            }
        }
    }

}