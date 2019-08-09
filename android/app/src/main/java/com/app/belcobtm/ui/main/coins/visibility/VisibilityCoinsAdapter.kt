package com.app.belcobtm.ui.main.coins.visibility

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.db.DbCryptoCoin
import kotlinx.android.synthetic.main.item_coin.view.coin_image
import kotlinx.android.synthetic.main.item_coin.view.coin_name
import kotlinx.android.synthetic.main.item_coin_visible.view.*


class VisibilityCoinsAdapter(
    private val mCoinsList: ArrayList<DbCryptoCoin>,
    private val mVisibilityChangedListener: OnCoinVisibilityChangedListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coin_visible, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position < mCoinsList.size) {
            val item = mCoinsList[position]

            val coinImageId = when (item.coinType) {
                "ETH" -> R.drawable.ic_eth_logo
                "BCH" -> R.drawable.ic_bch_logo
                "LTC" -> R.drawable.ic_ltc_logo
                "BNB" -> R.drawable.ic_bnb_logo
                "TRX" -> R.drawable.ic_trx_logo
                "XRP" -> R.drawable.ic_xrp_logo
                else -> R.drawable.ic_bit_logo
            }
            holder.itemView.coin_image.setImageResource(coinImageId)
            holder.itemView.coin_name.text = item.coinType
            holder.itemView.coin_visible.setOnCheckedChangeListener { buttonView, isChecked ->
                item.visible = isChecked

                val context = buttonView.context
                buttonView.text = if (isChecked) context.getString(R.string.hide)
                else context.getString(R.string.show)

                mVisibilityChangedListener.onCoinVisibilityChanged(position, isChecked)
            }
            holder.itemView.coin_visible.isChecked = item.visible

        }
    }

    override fun getItemCount(): Int {
        return mCoinsList.size
    }

    interface OnCoinVisibilityChangedListener {
        fun onCoinVisibilityChanged(position: Int, visibility: Boolean)
    }
}
