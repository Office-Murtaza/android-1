package com.belcobtm.presentation.features.wallet.add.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.databinding.ItemCoinVisibleBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.tools.extensions.resIcon

class AddWalletCoinsAdapter(
    private val listener: (position: Int, isChecked: Boolean) -> Unit
) : RecyclerView.Adapter<AddWalletCoinsAdapter.Holder>() {

    private val itemList: MutableList<AddWalletCoinItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemCoinVisibleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemList[position]
        with(holder.binding) {
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

    class Holder(
        val binding: ItemCoinVisibleBinding,
        listener: (position: Int, isChecked: Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.switchView.setOnClickListener {
                listener.invoke(adapterPosition, binding.switchView.isChecked)
            }
        }

    }

}
