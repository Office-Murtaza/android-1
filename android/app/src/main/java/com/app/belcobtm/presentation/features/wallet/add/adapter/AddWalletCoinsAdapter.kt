package com.app.belcobtm.presentation.features.wallet.add.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.resIcon
import kotlinx.android.synthetic.main.item_coin_visible.view.*


class AddWalletCoinsAdapter(private val listener: (position: Int, isChecked: Boolean) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemList: MutableList<AddWalletCoinItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin_visible, parent, false)
        val holder = Holder(view)
        view.switchView.setOnClickListener {
            listener.invoke(holder.adapterPosition, view.switchView.isChecked)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        with(holder.itemView) {
            val coinType = LocalCoinType.valueOf(item.coinCode)
            imageView.setImageResource(coinType.resIcon())
            titleView.text = coinType.fullName
            switchView.isChecked = item.isChecked
        }
    }

    fun setItemList(itemList: List<AddWalletCoinItem>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
