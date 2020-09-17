package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.app.belcobtm.R
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.extensions.resIcon
import kotlinx.android.synthetic.main.item_coin_to_coin.view.*

class CoinDialogAdapter(
    context: Context,
    itemList: List<LocalCoinType>
) :
    ArrayAdapter<LocalCoinType>(context, R.layout.item_coin_to_coin, itemList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_coin_to_coin, parent, false)
        getItem(position)?.let { coinType ->
            view.imageView.setImageResource(coinType.resIcon())
            view.textView.text = coinType.fullName
        }
        return view
    }
}