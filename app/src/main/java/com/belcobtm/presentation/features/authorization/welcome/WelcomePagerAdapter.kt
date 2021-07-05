package com.belcobtm.presentation.features.authorization.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.belcobtm.R
import com.belcobtm.databinding.ItemWelcomePagerBinding

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
    ): Holder = Holder(
        ItemWelcomePagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.imageView.setImageResource(itemList[position].imageRes)
        holder.binding.textView.text = holder.itemView.context.getString(itemList[position].nameRes)
    }

    class Holder(val binding: ItemWelcomePagerBinding) : RecyclerView.ViewHolder(binding.root)
}