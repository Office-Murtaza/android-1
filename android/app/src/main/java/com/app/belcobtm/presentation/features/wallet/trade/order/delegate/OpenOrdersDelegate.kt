package com.app.belcobtm.presentation.features.wallet.trade.order.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.databinding.ItemOpenOrderBinding
import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.setDrawableEnd
import com.app.belcobtm.presentation.core.extensions.setDrawableStart
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class OpenOrdersDelegate(
    private val onOrdersClickListener: (OrderItem) -> Unit
) : AdapterDelegate<OrderItem, OpenOrdersViewHolder>() {
    override val viewType: Int
        get() = OrderItem.OPEN_ORDER_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): OpenOrdersViewHolder =
        OpenOrdersViewHolder(onOrdersClickListener, ItemOpenOrderBinding.inflate(inflater, parent, false))
}

class OpenOrdersViewHolder(
    onOrdersClickListener: (OrderItem) -> Unit,
    private val binding: ItemOpenOrderBinding
) : MultiTypeViewHolder<OrderItem>(binding.root) {

    private val paymentAdapter = MultiTypeAdapter()

    init {
        paymentAdapter.registerDelegate(TradePaymentOptionDelegate())
        binding.paymentOptions.adapter = paymentAdapter
        binding.root.setOnClickListener {
            onOrdersClickListener(model)
        }
    }

    override fun bind(model: OrderItem) {
        with(model) {
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.cryptoAmountValue.text = binding.root.resources.getString(
                R.string.item_open_orders_crypto_amount_formatted, cryptoAmount.toStringCoin(), coin.name
            )
            binding.fiatValue.text = fiatAmountFormatted
            binding.priceLabel.text = priceFormatted
            binding.orderStatus.setText(orderStatus.statusLabelId)
            binding.orderStatus.setDrawableEnd(orderStatus.statusDrawableId)
            paymentAdapter.update(paymentOptions)
            if (mappedTradeType == TradeType.BUY) {
                binding.tradeType.setBackgroundResource(R.drawable.trade_type_buy_background)
                binding.tradeType.setDrawableStart(R.drawable.ic_trade_type_buy)
                binding.tradeType.setText(R.string.trade_type_buy_label)
                binding.tradeType.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.trade_type_buy_trade_text_color)
                )
            } else {
                binding.tradeType.setBackgroundResource(R.drawable.trade_type_sell_background)
                binding.tradeType.setDrawableStart(R.drawable.ic_trade_type_sell)
                binding.tradeType.setText(R.string.trade_type_sell_label)
                binding.tradeType.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.trade_type_sell_trade_text_color)
                )
            }
        }
    }
}