package com.app.belcobtm.ui.main.coins.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import kotlinx.android.synthetic.main.item_coin.view.*


class CoinsAdapter(
    private val mCoinsList: ArrayList<CoinModel>,
    private val mOnCoinClickListener: OnCoinClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mCoinsList.size) {
            val item = mCoinsList[position]

            val coinImageId = when (item.coinId) {
                "ETH" -> R.drawable.ic_coin_ethereum
                "BCH" -> R.drawable.ic_coin_bitcoin_cash
                "LTC" -> R.drawable.ic_coin_litecoin
                "BNB" -> R.drawable.ic_coin_binance
                "TRX" -> R.drawable.ic_coin_tron
                "XRP" -> R.drawable.ic_coin_ripple
                else -> R.drawable.ic_coin_bitcoin
            }
            holder.itemView.coin_image.setImageResource(coinImageId)
            holder.itemView.coin_name.text = item.fullCoinName
            holder.itemView.coin_balance.text = "${item.balance.toStringCoin()} ${item.coinId}"
            holder.itemView.coin_price.text = "USD ${item.price?.uSD}"
            holder.itemView.coin_container.setOnClickListener { mOnCoinClickListener.onCoinClick(item, mCoinsList) }
        }

    }

    override fun getItemCount(): Int {
        return mCoinsList.size
    }

    interface OnCoinClickListener {
        fun onCoinClick(coin: CoinModel, coinArray: List<CoinModel>)
    }
}
