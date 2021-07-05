package com.belcobtm.presentation.core.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class SpaceDecorator(
    private val marginTop: Int,
    private val marginBottom: Int,
    private val marginStart: Int,
    private val marginEnd: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = marginStart
        outRect.right = marginEnd
        outRect.top = marginTop
        outRect.bottom = marginBottom
    }
}
