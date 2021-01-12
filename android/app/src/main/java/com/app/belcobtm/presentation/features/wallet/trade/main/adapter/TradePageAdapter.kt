package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemTradePageBinding
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate.TradeDetailsEmptyDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate.TradeDetailsErrorDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate.TradeDetailsItemDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.adapter.delegate.TradeDetailsLoadingDelegate
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import com.app.belcobtm.presentation.features.wallet.trade.main.type.TradeTabType

class TradePageAdapter(
    private val listener: (listItem: TradeDetailsItem) -> Unit,
    private val endListListener: (tabIndex: Int, currentListSize: Int) -> Unit
) : RecyclerView.Adapter<TradePageAdapter.Holder>() {

    val adapterList: List<TradeListAdapter> = listOf(
        TradeListAdapter { endListListener.invoke(TradeTabType.BUY.ordinal, it) },
        TradeListAdapter { endListListener.invoke(TradeTabType.SELL.ordinal, it) },
        TradeListAdapter { endListListener.invoke(TradeTabType.MY.ordinal, it) },
        TradeListAdapter { endListListener.invoke(TradeTabType.OPEN.ordinal, it) }
    )

    init {
        adapterList.forEach { adapter ->
            adapter.registerDelegates(listener)
        }
    }

    private fun TradeListAdapter.registerDelegates(listener: (listItem: TradeDetailsItem) -> Unit) {
        registerDelegate(TradeDetailsLoadingDelegate())
        registerDelegate(TradeDetailsErrorDelegate())
        registerDelegate(TradeDetailsEmptyDelegate())
        registerDelegate(TradeDetailsItemDelegate(listener))
    }

    override fun getItemCount(): Int = adapterList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTradePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val dividerItemDecoration =
            DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(parent.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.listView.addItemDecoration(dividerItemDecoration)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.listView.adapter = adapterList[position]
    }

    fun setBuyList(itemList: List<ListItem>) {
        this.adapterList[TradeTabType.BUY.ordinal].update(itemList)
    }

    fun setSellList(itemList: List<ListItem>) {
        this.adapterList[TradeTabType.SELL.ordinal].update(itemList)
    }

    fun setMyList(itemList: List<ListItem>) {
        this.adapterList[TradeTabType.MY.ordinal].update(itemList)
    }

    fun setOpenList(itemList: List<ListItem>) {
        this.adapterList[TradeTabType.OPEN.ordinal].update(itemList)
    }

    fun clearData() {
        this.adapterList.forEach { it.clearItemList() }
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemTradePageBinding) : RecyclerView.ViewHolder(binding.root)
}