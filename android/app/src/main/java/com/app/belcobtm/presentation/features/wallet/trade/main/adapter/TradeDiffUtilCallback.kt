package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import androidx.recyclerview.widget.DiffUtil
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem

class TradeDiffUtilCallback(
    private val oldList: List<TradeDetailsItem>,
    private val newList: List<TradeDetailsItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = getIdByItem(oldList[oldItemPosition]) == getIdByItem(newList[newItemPosition])

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean = oldList[oldItemPosition] == newList[newItemPosition]

    private fun getIdByItem(item: TradeDetailsItem): Int = when (item) {
        is TradeDetailsItem.BuySell -> item.id
        is TradeDetailsItem.Open -> item.id
        is TradeDetailsItem.My -> item.id
        else -> -1
    }
}