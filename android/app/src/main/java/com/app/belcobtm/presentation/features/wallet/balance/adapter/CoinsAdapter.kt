package com.app.belcobtm.presentation.features.wallet.balance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import kotlinx.android.synthetic.main.item_balance_coin.view.*

class CoinsAdapter(private val listener: (item: BalanceListItem) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemList: MutableList<BalanceListItem> = mutableListOf(
        BalanceListItem.AddButton
    )

    override fun getItemCount(): Int = itemList.size

    override fun getItemViewType(position: Int): Int = when (itemList[position]) {
        is BalanceListItem.Coin -> R.layout.item_balance_coin
        is BalanceListItem.AddButton -> R.layout.item_balance_button_manage_wallets
    }

    override fun onCreateViewHolder(parent: ViewGroup, layout: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        val holder = Holder(view)
        when (layout) {
            R.layout.item_balance_coin -> {
                view.containerView.setOnClickListener { listener.invoke(itemList[holder.adapterPosition]) }
            }
            R.layout.item_balance_button_manage_wallets -> {
                view.setOnClickListener { listener.invoke(itemList[holder.adapterPosition]) }
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (item is BalanceListItem.Coin) {
            with(holder.itemView) {
                imageView.setImageResource(LocalCoinType.valueOf(item.code).resIcon())
                nameView.text = LocalCoinType.valueOf(item.code).fullName
                balanceCryptoView.text =
                    context.getString(R.string.unit_dynamic, item.balanceCrypto.toStringCoin(), item.code)
                balanceFiatView.text =
                    context.getString(R.string.unit_usd_dynamic_symbol, item.balanceCrypto.toStringUsd())
                priceView.text = context.getString(R.string.unit_usd_dynamic_symbol, item.priceUsd.toStringUsd())
            }
        }
    }

    fun setItemList(itemList: List<BalanceListItem.Coin>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        this.itemList.add(BalanceListItem.AddButton)
        notifyDataSetChanged()
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
