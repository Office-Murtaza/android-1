package com.app.belcobtm.presentation.features.wallet.trade.main.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.features.wallet.trade.main.item.TradeDetailsItem
import kotlinx.android.synthetic.main.item_trade_list.view.*

class TradeListAdapter(
    private val listener: (listItem: TradeDetailsItem) -> Unit
) : RecyclerView.Adapter<TradeListAdapter.Holder>() {
    private val itemList: MutableList<TradeDetailsItem> = mutableListOf(TradeDetailsItem.Empty)

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int = when (itemList[position]) {
        is TradeDetailsItem.Empty -> R.layout.item_empty
        is TradeDetailsItem.Loading -> R.layout.item_progress
        is TradeDetailsItem.Error -> R.layout.item_error
        else -> R.layout.item_trade_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, layout: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = Holder(view)
        view.setOnClickListener { listener.invoke(itemList[holder.adapterPosition]) }
        return holder
    }

    override fun onBindViewHolder(
        holder: Holder,
        position: Int
    ) {
        when (val item = itemList[position]) {
            is TradeDetailsItem.BuySell -> fillDefaultItem(
                holder.itemView,
                item.minLimit,
                item.maxLimit,
                item.userName,
                item.tradeCount,
                item.rate,
                item.distance,
                item.paymentMethod,
                item.price
            )
            is TradeDetailsItem.Open -> fillDefaultItem(
                holder.itemView,
                item.minLimit,
                item.maxLimit,
                item.userName,
                item.tradeCount,
                item.rate,
                item.distance,
                item.paymentMethod,
                item.price
            )
            is TradeDetailsItem.My -> fillDefaultItem(
                holder.itemView,
                item.minLimit,
                item.maxLimit,
                item.userName,
                item.tradeCount,
                item.rate,
                item.distance,
                item.paymentMethod,
                item.price
            )
        }

//        if (item is TradeDetailsItem.Open) {
//            view.tradeTypeView.background.setTint(Color.RED)
//            view.tradeTypeView.show()
//        } else {
//            view.tradeTypeView.hide()
//        }
    }

    fun setItemList(itemList: List<TradeDetailsItem>) {
        this.itemList.clear()
        if (itemList.isEmpty()) {
            this.itemList.add(TradeDetailsItem.Empty)
        } else {
            this.itemList.addAll(itemList)
        }
        notifyDataSetChanged()
    }

    private fun fillDefaultItem(
        view: View,
        minLimit: Int,
        maxLimit: Int,
        userName: String,
        tradeCount: Int,
        rate: Double,
        distance: Int,
        paymentMethod: String,
        price: Double
    ) {
        view.userNameView.text =
            view.context.getString(R.string.trade_screen_user_field, userName, tradeCount, rate.toString(), distance)
        view.paymentMethodView.text = paymentMethod
        view.priceView.text = view.context.getString(R.string.unit_usd_dynamic, price.toStringUsd())
        view.priceLimitView.text = view.context.getString(R.string.unit_usd_dynamic, "$minLimit - $maxLimit")
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}