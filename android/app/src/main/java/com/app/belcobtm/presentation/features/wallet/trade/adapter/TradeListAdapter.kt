package com.app.belcobtm.presentation.features.wallet.trade.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.features.wallet.trade.item.TradeListItem
import kotlinx.android.synthetic.main.item_trade_buy.view.*

class TradeListAdapter : RecyclerView.Adapter<TradeListAdapter.Holder>() {
    private val itemList: MutableList<TradeListItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int = when (itemList[position]) {
        is TradeListItem.Buy -> R.layout.item_trade_buy
    }

    override fun onCreateViewHolder(parent: ViewGroup, layout: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = Holder(view)
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        when (val item = itemList[position]) {
            is TradeListItem.Buy -> with(holder.itemView) {
                userNameView.text = item.userName
                paymentMethodView.text = item.paymentMethod
                priceView.text = item.price
                priceLimitView.text = item.paymentMethod
            }
        }
    }

    fun setItemList(itemList: List<TradeListItem>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}