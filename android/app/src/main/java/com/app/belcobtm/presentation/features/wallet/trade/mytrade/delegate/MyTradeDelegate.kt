package com.app.belcobtm.presentation.features.wallet.trade.mytrade.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.ItemMyTradeBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradeItem

class MyTradeDelegate : AdapterDelegate<TradeItem, MyTradeViewHolder>() {
    override val viewType: Int
        get() = TradeItem.TRADE_ITEM_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): MyTradeViewHolder =
        MyTradeViewHolder(ItemMyTradeBinding.inflate(inflater, parent, false))
}

class MyTradeViewHolder(private val binding: ItemMyTradeBinding) : MultiTypeViewHolder<TradeItem>(binding.root) {

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
            binding.priceRange.text = binding.root.context.getString(
                R.string.trade_list_item_price_range_format,
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, minLimit.toStringUsd()),
                binding.root.context.getString(R.string.trade_list_item_usd_formatted, maxLimit.toStringUsd())
            )
            binding.priceLabel.text = binding.root.context.getString(
                R.string.trade_list_item_usd_formatted, price.toStringUsd()
            )
            paymentAdapter.update(paymentMethods)
            if(tradeType == TradeType.BUY) {
                binding.tradeType.setBackgroundResource(R.drawable.trade_type_buy_background)
                val tradeTypeDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_trade_type_buy)
                binding.tradeType.setCompoundDrawables(tradeTypeDrawable, null, null, null)
                binding.tradeType.setText(R.string.trade_type_buy_label)
            } else {
                binding.tradeType.setBackgroundResource(R.drawable.trade_type_sell_background)
                val tradeTypeDrawable = ContextCompat.getDrawable(binding.root.context, R.drawable.ic_trade_type_sell)
                binding.tradeType.setCompoundDrawables(tradeTypeDrawable, null, null, null)
                binding.tradeType.setText(R.string.trade_type_sell_label)
            }
        }
    }

}