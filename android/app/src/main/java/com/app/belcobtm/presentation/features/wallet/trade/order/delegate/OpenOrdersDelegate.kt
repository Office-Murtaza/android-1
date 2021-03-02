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
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.features.wallet.trade.list.delegate.TradePaymentOptionDelegate
import com.app.belcobtm.presentation.features.wallet.trade.list.model.OrderItem

class OpenOrdersDelegate : AdapterDelegate<OrderItem, OpenOrdersViewHolder>() {
    override val viewType: Int
        get() = OrderItem.OPEN_ORDER_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): OpenOrdersViewHolder =
        OpenOrdersViewHolder(ItemOpenOrderBinding.inflate(inflater, parent, false))
}

class OpenOrdersViewHolder(
    private val binding: ItemOpenOrderBinding
) : MultiTypeViewHolder<OrderItem>(binding.root) {

    private val paymentAdapter = MultiTypeAdapter()

    init {
        paymentAdapter.registerDelegate(TradePaymentOptionDelegate())
        binding.paymentOptions.adapter = paymentAdapter
        binding.root.setOnClickListener {
            // TODO open order details
        }
    }

    override fun bind(model: OrderItem) {
        with(model) {
            binding.coinIcon.setImageResource(coin.resIcon())
            binding.coinCode.text = coin.name
            binding.cryptoAmountValue.text = cryptoAmount.toStringCoin()
            binding.fiatValue.text = fiatAmount.toStringUsd()
            binding.priceLabel.text = binding.root.context.getString(
                R.string.trade_list_item_usd_formatted, price.toStringUsd()
            )
            binding.orderStatus.setText(statusLabelId)
            val statusDrawable = ContextCompat.getDrawable(binding.root.context, statusDrawableId)
            binding.orderStatus.setCompoundDrawables(null, null, statusDrawable, null)
            paymentAdapter.update(paymentOptions)
            if (tradeType == TradeType.BUY) {
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