package com.belcobtm.presentation.screens.deals.swap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.belcobtm.R
import com.belcobtm.databinding.ItemCoinToCoinBinding
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.tools.extensions.resIcon

class CoinDialogAdapter(
    context: Context,
    itemList: List<CoinDataItem>
) : ArrayAdapter<CoinDataItem>(context, R.layout.item_coin_to_coin, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding = if (convertView != null) {
            convertView.tag as ItemCoinToCoinBinding
        } else {
            ItemCoinToCoinBinding.inflate(LayoutInflater.from(context), parent, false).apply {
                root.tag = this
            }
        }
        getItem(position)?.let { coin ->
            val localType = LocalCoinType.valueOf(coin.code)
            binding.imageView.setImageResource(localType.resIcon())
            binding.textView.text = localType.fullName
        }
        return binding.root
    }
}