package com.app.belcobtm.presentation.features.wallet.trade.list.filter.delegate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemTradeFilterCoinCodeBinding
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem

class ItemTradeFilterCoinCodeDelegate(
    private val onCoinCheckedListener: (CoinCodeListItem) -> Unit
) : AdapterDelegate<CoinCodeListItem, ItemTradeFilterCoinCodeViewHolder>() {
    override val viewType: Int
        get() = CoinCodeListItem.COIN_CODE_LIST_ITEM_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): ItemTradeFilterCoinCodeViewHolder =
        ItemTradeFilterCoinCodeViewHolder(
            ItemTradeFilterCoinCodeBinding.inflate(inflater, parent, false), onCoinCheckedListener
        )
}

@SuppressLint("ClickableViewAccessibility")
class ItemTradeFilterCoinCodeViewHolder(
    private val binding: ItemTradeFilterCoinCodeBinding,
    private val onCoinCheckedListener: (CoinCodeListItem) -> Unit
) : MultiTypeViewHolder<CoinCodeListItem>(binding.root) {

    init {
        binding.coin.setOnTouchListener { _, _ ->
            onCoinCheckedListener(model)
            true
        }
    }

    override fun bind(model: CoinCodeListItem) {
        binding.coin.isEnabled = model.enabled
        binding.coin.isChecked = model.selected
        binding.coin.setChipIconResource(model.coinIcon)
        binding.coin.text = model.coinCode
        binding.coin.chipStrokeWidth = if (model.selected) {
            binding.root.resources.getDimensionPixelSize(R.dimen.divider_size).toFloat()
        } else {
            0.0f
        }
    }
}