package com.app.belcobtm.presentation.features.authorization.welcome

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import kotlinx.android.synthetic.main.item_welcome_pager.view.*

class WelcomePagerAdapter : RecyclerView.Adapter<WelcomePagerAdapter.Holder>() {
    private val itemList: List<WelcomePagerItem> = listOf(
        WelcomePagerItem(
            R.drawable.ic_welcome_slide_1,
            R.string.welcome_screen_all_assets_in_one_place
        ),
        WelcomePagerItem(
            R.drawable.ic_welcome_slide_2,
            R.string.welcome_screen_private_and_secure
        ),
        WelcomePagerItem(
            R.drawable.ic_welcome_slide_3,
            R.string.welcome_screen_buy_sell_and_trade_assets
        )
    )

    override fun getItemCount(): Int = itemList.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Holder = Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_welcome_pager, parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.itemView.imageView.setImageResource(itemList[position].imageRes)
        holder.itemView.textView.text = holder.itemView.context.getString(itemList[position].nameRes)
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view)
}