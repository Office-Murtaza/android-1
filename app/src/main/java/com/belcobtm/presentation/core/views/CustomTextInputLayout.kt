package com.belcobtm.presentation.core.views

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.belcobtm.R
import com.google.android.material.textfield.TextInputLayout

class CustomTextInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextInputLayout(context, attrs, defStyleAttr) {

    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)
        findViewById<TextView>(R.id.textinput_error)?.let{
            it.textAlignment = TextView.TEXT_ALIGNMENT_VIEW_END
        }
    }
}