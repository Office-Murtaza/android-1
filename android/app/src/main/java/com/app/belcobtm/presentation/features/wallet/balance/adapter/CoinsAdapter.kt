package com.app.belcobtm.presentation.features.wallet.balance.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import kotlinx.android.synthetic.main.item_balance_coin.view.*

class CoinsAdapter(
    private val listener: (item: CoinListItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val diffCallback = BalanceDiffUtil()
    private val itemList: MutableList<CoinListItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_balance_coin, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
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

    fun setItemList(itemList: List<CoinListItem>) {
        diffCallback.oldList = this.itemList
        diffCallback.newList = itemList
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.itemList.clear()
        this.itemList.addAll(itemList)
        diffResult.dispatchUpdatesTo(this)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
