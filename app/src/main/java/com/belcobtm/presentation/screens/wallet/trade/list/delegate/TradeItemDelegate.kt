package com.belcobtm.presentation.screens.wallet.trade.list.delegate

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemTradeBinding
import com.belcobtm.domain.trade.model.trade.TradeDomainModel.Companion.UNDEFINED_DISTANCE
import com.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.toHtmlSpan

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
            val context = binding.root.context
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.makerPublicId.text = makerPublicId
            binding.makerPublicId.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_account_circle, 0, 0, 0
            )
            if (model.minLimit > model.maxLimit) {
                with(binding.priceRange) {
                    text = context.getString(R.string.trade_amount_range_out_of_stock)
                    setTextColor(ContextCompat.getColor(context, R.color.colorError))
                    setTypeface(typeface, Typeface.BOLD)
                }
            } else {
                with(binding.priceRange) {
                    text = context.getString(
                        R.string.trade_list_item_price_range_format,
                        minLimitFormatted,
                        maxLimitFormatted
                    )
                    setTextColor(ContextCompat.getColor(context, R.color.black_text_color))
                    setTypeface(typeface, Typeface.NORMAL)
                }
            }
            binding.priceLabel.text = priceFormatted
            binding.makerTradeCountLabel.text = makerTotalTradesFormatted
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