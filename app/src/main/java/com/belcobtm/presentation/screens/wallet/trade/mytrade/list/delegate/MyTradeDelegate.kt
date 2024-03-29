package com.belcobtm.presentation.screens.wallet.trade.mytrade.list.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.belcobtm.R
import com.belcobtm.domain.trade.model.trade.TradeStatus
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.databinding.ItemMyTradeBinding
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.screens.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem

class MyTradeDelegate(
    private val onTradeDeletedClick: (TradeItem) -> Unit,
    private val onTradeDetailsClick: (TradeItem) -> Unit
) : AdapterDelegate<TradeItem, MyTradeViewHolder>() {
    override val viewType: Int
        get() = TradeItem.TRADE_ITEM_LIST_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): MyTradeViewHolder =
        MyTradeViewHolder(
            ItemMyTradeBinding.inflate(inflater, parent, false),
            onTradeDeletedClick, onTradeDetailsClick
        )
}

class MyTradeViewHolder(
    private val binding: ItemMyTradeBinding,
    onTradeDeletedClick: (TradeItem) -> Unit,
    onTradeDetailsClick: (TradeItem) -> Unit
) : MultiTypeViewHolder<TradeItem>(binding.root) {

    private val paymentAdapter = MultiTypeAdapter()

    init {
        paymentAdapter.registerDelegate(TradePaymentOptionDelegate())
        binding.paymentOptions.adapter = paymentAdapter
        binding.myTradeRoot.setOnClickListener {
            onTradeDetailsClick(model)
        }
        binding.deleteTrade.setOnClickListener {
            onTradeDeletedClick(model)
        }
    }

    override fun bind(model: TradeItem) {
        with(model) {
            val context = binding.root.context
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.priceRange.text = if (model.minLimit > model.maxLimit) {
                context.getString(R.string.trade_amount_range_out_of_stock)
            } else {
                context.getString(
                    R.string.trade_list_item_price_range_format,
                    minLimitFormatted,
                    maxLimitFormatted
                )
            }
            binding.priceLabel.text = priceFormatted
            paymentAdapter.update(paymentMethods)
            with(binding.tradeType) {
                if (tradeType == TradeType.BUY) {
                    setBackgroundResource(R.drawable.trade_type_buy_background)
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trade_type_buy, 0, 0, 0)
                    setText(R.string.trade_type_buy_label)
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.secondaryGreen
                        )
                    )
                } else {
                    setBackgroundResource(R.drawable.trade_type_sell_background)
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_trade_type_sell, 0, 0, 0)
                    setText(R.string.trade_type_sell_label)
                    setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.trade_type_sell_trade_text_color
                        )
                    )
                }
            }
            if(model.status == TradeStatus.CANCELED) {
                binding.myTradeRoot.children.forEach {
                    it.alpha = 0.5f
                }
            } else {
                binding.myTradeRoot.children.forEach {
                    it.alpha = 1f
                }
            }
        }
    }

}