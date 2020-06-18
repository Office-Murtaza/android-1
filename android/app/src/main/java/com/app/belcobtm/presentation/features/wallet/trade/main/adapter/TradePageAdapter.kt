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
    private val listener: (listItem: TradeDetailsItem) -> Unit,
    private val endListListener: (tabIndex: Int, currentListSize: Int) -> Unit
) :
    RecyclerView.Adapter<TradePageAdapter.Holder>() {
    val adapterList: List<TradeListAdapter> = listOf(
        TradeListAdapter({ listener.invoke(it) }, { endListListener.invoke(TradeTabType.BUY.ordinal, it) }),
        TradeListAdapter({ listener.invoke(it) }, { endListListener.invoke(TradeTabType.SELL.ordinal, it) }),
        TradeListAdapter({ listener.invoke(it) }, { endListListener.invoke(TradeTabType.MY.ordinal, it) }),
        TradeListAdapter({ listener.invoke(it) }, { endListListener.invoke(TradeTabType.OPEN.ordinal, it) })
    )

    override fun getItemCount(): Int = adapterList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_trade_page, parent, false)
        val dividerItemDecoration =
            DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(parent.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        view.listView.addItemDecoration(dividerItemDecoration)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.listView.adapter = adapterList[position]
    }

    fun setBuyList(itemList: List<TradeDetailsItem>) {
        this.adapterList[TradeTabType.BUY.ordinal].setItemList(itemList)
    }

    fun setSellList(itemList: List<TradeDetailsItem>) {
        this.adapterList[TradeTabType.SELL.ordinal].setItemList(itemList)
    }

    fun setMyList(itemList: List<TradeDetailsItem>) {
        this.adapterList[TradeTabType.MY.ordinal].setItemList(itemList)
    }

    fun setOpenList(itemList: List<TradeDetailsItem>) {
        this.adapterList[TradeTabType.OPEN.ordinal].setItemList(itemList)
    }

    fun clearData() {
        this.adapterList.forEach { it.clearItemList() }
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}