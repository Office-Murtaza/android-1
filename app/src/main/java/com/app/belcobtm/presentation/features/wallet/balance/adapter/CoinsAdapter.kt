package com.app.belcobtm.presentation.features.wallet.balance.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemBalanceCoinBinding
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.resIcon
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.extensions.toStringUsd

class CoinsAdapter(
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
            balanceFiatView.text =
                root.context.getString(R.string.text_usd, item.balanceFiat.toStringUsd())
            priceView.text = root.context.getString(R.string.text_usd, item.priceUsd.toStringUsd())
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
