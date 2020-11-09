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
    private val itemList: MutableList<BalanceListItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_balance_coin, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        if (item is BalanceListItem.Coin) {
            with(holder.itemView) {
                imageView.setImageResource(LocalCoinType.valueOf(item.code).resIcon())
                nameView.text = LocalCoinType.valueOf(item.code).fullName
                balanceCryptoView.text =
                    context.getString(
                        R.string.text_text,
                        item.balanceCrypto.toStringCoin(),
                        item.code
                    )
                balanceFiatView.text =
                    context.getString(R.string.text_usd, item.balanceFiat.toStringUsd())
                priceView.text = context.getString(R.string.text_usd, item.priceUsd.toStringUsd())
                setOnClickListener {
                    listener.invoke(itemList[holder.adapterPosition])
                }
            }
        }
    }

    fun setItemList(itemList: List<BalanceListItem.Coin>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
