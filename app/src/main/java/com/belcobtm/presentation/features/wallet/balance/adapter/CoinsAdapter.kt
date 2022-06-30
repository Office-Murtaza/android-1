package com.belcobtm.presentation.features.wallet.balance.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.R
import com.belcobtm.databinding.ItemBalanceCoinBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.tools.extensions.resIcon
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.tools.formatter.Formatter

class CoinsAdapter(
    private val priceFormatter: Formatter<Double>,
    private val listener: (item: CoinListItem) -> Unit
) : RecyclerView.Adapter<CoinsAdapter.Holder>() {

    private val diffCallback = BalanceDiffUtil()
    private val itemList: MutableList<CoinListItem> = mutableListOf()

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBalanceCoinBinding.inflate(inflater, parent, false)
        return Holder(binding, listener)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = itemList[position]
        holder.item = item
        with(holder.binding) {
            imageView.setImageResource(LocalCoinType.valueOf(item.code).resIcon())
            nameView.text = LocalCoinType.valueOf(item.code).fullName
            balanceCryptoView.text =
                root.context.getString(
                    R.string.text_text,
                    item.balanceCrypto.toStringCoin(),
                    item.code
                )
            balanceFiatView.text = priceFormatter.format(item.balanceFiat)
            priceView.text = priceFormatter.format(item.priceUsd)
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

    class Holder(
        val binding: ItemBalanceCoinBinding,
        listener: (item: CoinListItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        lateinit var item: CoinListItem

        init {
            binding.root.setOnClickListener {
                listener.invoke(item)
            }
        }
    }
}
