package com.belcobtm.presentation.features.wallet.balance.adapter

import androidx.recyclerview.widget.DiffUtil

class BalanceDiffUtil : DiffUtil.Callback() {

    var oldList: List<CoinListItem> = emptyList()
    var newList: List<CoinListItem> = emptyList()

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].code == newList[newItemPosition].code

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

}