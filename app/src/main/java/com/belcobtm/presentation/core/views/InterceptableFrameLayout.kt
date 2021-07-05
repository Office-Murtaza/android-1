package com.belcobtm.presentation.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class InterceptableFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var interceptListener: OnInterceptEventListener? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return super.onInterceptTouchEvent(ev) || handleInterception(ev)
    }

    private fun handleInterception(ev: MotionEvent?): Boolean {
        if (ev != null) interceptListener?.onTouchIntercented(ev)
        return false
    }

    interface OnInterceptEventListener {

        fun onTouchIntercented(ev: MotionEvent)
    }
}
