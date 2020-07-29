package com.app.belcobtm.presentation.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.toggle
import kotlinx.android.synthetic.main.item_settings.view.*


class SettingsItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.item_settings, this)

        val typedArray =
            context
                .theme
                .obtainStyledAttributes(
                    attrs,
                    R.styleable.SettingsItemView,
                    0,
                    0
                )
        val outValue = TypedValue()
        context
            .theme
            .resolveAttribute(
                android.R.attr.selectableItemBackground,
                outValue,
                true
            )
        setBackgroundResource(outValue.resourceId)
        try {
            setImage(typedArray.getDrawable(R.styleable.SettingsItemView_src))
            setLabel(typedArray.getString(R.styleable.SettingsItemView_text))
        } finally {
            typedArray.recycle()
        }
    }

    fun setImage(imageRes: Drawable?) {
        imageRes?.let {
            imageView.setImageDrawable(it)
            imageView.toggle(true)
        }
    }

    fun setLabel(label: CharSequence?) {
        labelText.text = label ?: ""
    }

    fun setValue(value: CharSequence) {
        valueText.text = value
    }
}