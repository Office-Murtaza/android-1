package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import com.app.belcobtm.presentation.core.adapter.MultiTypeAdapter
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class TradeListAdapter(private val endListListener: (currentListSize: Int) -> Unit) : MultiTypeAdapter() {

    override fun onBindViewHolder(holder: MultiTypeViewHolder<*>, position: Int) {
        super.onBindViewHolder(holder, position)
        if (position >= content.size - 1) {
            endListListener.invoke(content.size)
        }
    }

    fun clearItemList() {
        content.clear()
    }
}