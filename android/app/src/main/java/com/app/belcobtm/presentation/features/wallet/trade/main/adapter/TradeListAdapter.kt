package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.hide
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeListItem
import kotlinx.android.synthetic.main.item_trade_list.view.*

class TradeListAdapter : RecyclerView.Adapter<TradeListAdapter.Holder>() {
    private val itemList: MutableList<TradeListItem> = mutableListOf(TradeListItem.Empty)

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int = when (itemList[position]) {
        is TradeListItem.Empty -> R.layout.item_trade_list_empty
        else -> R.layout.item_trade_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, layout: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = Holder(view)
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) = when (val item = itemList[position]) {
        is TradeListItem.Empty -> Unit
        else -> with(holder.itemView) {
            userNameView.text = item.userName
            paymentMethodView.text = item.paymentMethod
            priceView.text = context.getString(R.string.unit_usd_dynamic, item.price)
            priceLimitView.text = context.getString(R.string.unit_usd_dynamic, item.priceLimit)

            if (item is TradeListItem.Open) {
                holder.itemView.tradeTypeView.background.setTint(Color.RED)
                holder.itemView.tradeTypeView.show()
            } else {
                holder.itemView.tradeTypeView.hide()
            }
        }
    }

    fun setItemList(itemList: List<TradeListItem>) {
        this.itemList.clear()
        if (itemList.isEmpty()) {
            this.itemList.add(TradeListItem.Empty)
        } else {
            this.itemList.addAll(itemList)
        }
        notifyDataSetChanged()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}