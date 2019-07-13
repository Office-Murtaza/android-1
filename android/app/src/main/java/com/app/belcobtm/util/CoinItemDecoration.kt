package com.app.belcobtm.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CoinItemDecoration(private val spacing: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

//        val itemCount = state.itemCount
        val position = parent.getChildAdapterPosition(view)

        if (position == 0)
            outRect.top = spacing
    }
}