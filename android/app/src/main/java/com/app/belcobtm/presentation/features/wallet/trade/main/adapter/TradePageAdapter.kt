package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import com.app.belcobtm.presentation.features.wallet.trade.main.type.TradeTabType
import kotlinx.android.synthetic.main.item_trade_page.view.*

class TradePageAdapter(
    private val listener: (listItem: TradeDetailsItem) -> Unit
) :
    RecyclerView.Adapter<TradePageAdapter.Holder>() {
    val itemList: List<MutableList<TradeDetailsItem>> = listOf(
        mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf()
    )

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trade_page, parent, false)
        val dividerItemDecoration =
            DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(parent.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        view.listView.addItemDecoration(dividerItemDecoration)
        view.listView.adapter = TradeListAdapter { listener.invoke(it) }
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        (holder.itemView.listView.adapter as TradeListAdapter).setItemList(itemList[position])
    }

    fun setBuyList(itemList: List<TradeDetailsItem>) {
        this.itemList[TradeTabType.BUY.ordinal].clear()
        this.itemList[TradeTabType.BUY.ordinal].addAll(itemList)
        notifyItemChanged(TradeTabType.BUY.ordinal)
    }

    fun setSellList(itemList: List<TradeDetailsItem>) {
        this.itemList[TradeTabType.SELL.ordinal].clear()
        this.itemList[TradeTabType.SELL.ordinal].addAll(itemList)
        notifyItemChanged(TradeTabType.SELL.ordinal)
    }

    fun setMyList(itemList: List<TradeDetailsItem>) {
        this.itemList[TradeTabType.MY.ordinal].clear()
        this.itemList[TradeTabType.MY.ordinal].addAll(itemList)
        notifyItemChanged(TradeTabType.MY.ordinal)
    }

    fun setOpenList(itemList: List<TradeDetailsItem>) {
        this.itemList[TradeTabType.OPEN.ordinal].clear()
        this.itemList[TradeTabType.OPEN.ordinal].addAll(itemList)
        notifyItemChanged(TradeTabType.OPEN.ordinal)
    }

    fun clearData() {
        this.itemList.forEach { it.clear() }
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}