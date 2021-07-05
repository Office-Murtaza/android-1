package com.belcobtm.presentation.core.helper

import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View

class SimpleClickableSpan(
    private val onClick: (widget: View) -> Unit,
    private val updateDrawState: ((ds: TextPaint) -> Unit)? = null
) : ClickableSpan() {
    override fun onClick(widget: View) = onClick.invoke(widget)

    override fun updateDrawState(ds: TextPaint) {
        super.updateDrawState(ds)
        updateDrawState?.invoke(ds)
    }
}