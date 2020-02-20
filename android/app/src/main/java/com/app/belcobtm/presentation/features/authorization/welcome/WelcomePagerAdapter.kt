package com.app.belcobtm.presentation.features.authorization.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.model.WelcomePagerItem
import kotlinx.android.synthetic.main.item_welcome_pager.view.*

class WelcomePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val itemList: List<WelcomePagerItem> = listOf(
        WelcomePagerItem(R.drawable.ic_welcome_slide1, R.string.welcome_slide_1),
        WelcomePagerItem(R.drawable.ic_welcome_slide2, R.string.welcome_slide_2),
        WelcomePagerItem(R.drawable.ic_welcome_slide3, R.string.welcome_slide_3)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_welcome_pager, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.image.setImageResource(itemList[position].imageRes)
        holder.itemView.name.text = holder.itemView.context.getString(itemList[position].nameRes)
    }
}