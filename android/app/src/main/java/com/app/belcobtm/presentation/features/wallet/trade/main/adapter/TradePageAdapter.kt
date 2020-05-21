package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradePageItem
import kotlinx.android.synthetic.main.item_trade_page.view.*

class TradePageAdapter : RecyclerView.Adapter<TradePageAdapter.Holder>() {
    private val itemList: MutableList<TradePageItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trade_page, parent, false)
        val dividerItemDecoration = DividerItemDecoration(parent.context, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(parent.context, R.drawable.divider_transactions)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        view.listView.addItemDecoration(dividerItemDecoration)
        view.listView.adapter = TradeListAdapter()
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        (holder.itemView.listView.adapter as TradeListAdapter).setItemList(itemList[position].itemList)
    }

    fun setItemList(itemList: List<TradePageItem>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}