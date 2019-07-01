package com.app.belcobtm.ui.auth.welcome

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.belcobtm.R
import com.app.belcobtm.model.WelcomePagerItem
import kotlinx.android.synthetic.main.item_welcome_pager.view.*

class WelcomePagerAdapter(private val mValues: ArrayList<WelcomePagerItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_welcome_pager, parent, false)
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount(): Int {
        return 3
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder.itemView.image.setImageResource(mValues[position].imageRes)
        holder.itemView.name.text = holder.itemView.context.getString(mValues[position].nameRes)
    }
}