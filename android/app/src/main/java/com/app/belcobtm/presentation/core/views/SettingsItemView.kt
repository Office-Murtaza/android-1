package com.app.belcobtm.presentation.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ItemSettingsBinding
import com.app.belcobtm.presentation.core.extensions.toggle

class SettingsItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ItemSettingsBinding

    init {
        binding = ItemSettingsBinding.inflate(LayoutInflater.from(context), this)

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
            setValue(typedArray.getString(R.styleable.SettingsItemView_value))
            showChevron(typedArray.getBoolean(R.styleable.SettingsItemView_withChevron, false))
            showSwitch(typedArray.getBoolean(R.styleable.SettingsItemView_withSwitch, false))
        } finally {
            typedArray.recycle()
        }
    }

    fun setImage(imageRes: Drawable?) {
        imageRes?.let {
            binding.imageView.setImageDrawable(it)
            binding.imageView.toggle(true)
        }
    }

    fun setLabel(label: CharSequence?) {
        binding.labelText.text = label ?: ""
    }

    fun setValue(value: CharSequence?) {
        binding.valueText.text = value ?: ""
    }

    fun showChevron(show: Boolean) {
        binding.ivChevron.toggle(show)
    }

    fun showSwitch(show: Boolean) {
        binding.commonSwitch.toggle(show)
    }

    fun setSwitchState(swithed: Boolean) {
        binding.commonSwitch.isChecked = swithed
    }
}
