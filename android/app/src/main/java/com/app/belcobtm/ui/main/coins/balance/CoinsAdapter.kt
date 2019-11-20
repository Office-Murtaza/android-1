package com.app.belcobtm.ui.main.coins.balance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.api.model.response.CoinModel
import kotlinx.android.synthetic.main.item_coin.view.*


class CoinsAdapter(
    private val mCoinsList: ArrayList<CoinModel>,
    private val mOnCoinClickListener: OnCoinClickListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mCoinsList.size) {
            val item = mCoinsList[position]

            val coinImageId = when (item.coinId) {
                "ETH" -> R.drawable.ic_eth_logo
                "BCH" -> R.drawable.ic_bch_logo
                "LTC" -> R.drawable.ic_ltc_logo
                "BNB" -> R.drawable.ic_bnb_logo
                "TRX" -> R.drawable.ic_trx_logo
                "XRP" -> R.drawable.ic_xrp_logo
                else -> R.drawable.ic_bit_logo
            }
            holder.itemView.coin_image.setImageResource(coinImageId)
            holder.itemView.coin_name.text = item.coinId

            val balance = if (item.balance > 0)
                String.format("%.6f", item.balance).trimEnd('0')
            else "0"
            holder.itemView.coin_balance.text = "$balance ${item.coinId}"

            holder.itemView.coin_price.text = "USD ${item.price?.uSD}"
            holder.itemView.coin_container.setOnClickListener { mOnCoinClickListener.onCoinClick(item) }
        }

    }

    override fun getItemCount(): Int {
        return mCoinsList.size
    }

    interface OnCoinClickListener {
        fun onCoinClick(coin: CoinModel)
    }
}
