package com.belcobtm.presentation.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.belcobtm.R
import com.belcobtm.databinding.ItemSettingsBinding
import com.belcobtm.presentation.core.extensions.toggle

class SettingsItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding: ItemSettingsBinding = ItemSettingsBinding.inflate(LayoutInflater.from(context), this)

    init {
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
            setValueColor(typedArray.getColor(R.styleable.SettingsItemView_valueColor, ContextCompat.getColor(context, R.color.gray_text_color)))
            showChevron(typedArray.getBoolean(R.styleable.SettingsItemView_withChevron, false))
            showSwitch(typedArray.getBoolean(R.styleable.SettingsItemView_withSwitch, false))
        } finally {
            typedArray.recycle()
        }
    }

    private fun setImage(imageRes: Drawable?) {
        imageRes?.let {
            binding.imageView.setImageDrawable(it)
            binding.imageView.toggle(true)
        }
    }

    private fun setLabel(label: CharSequence?) {
        binding.labelText.text = label ?: ""
    }

    fun setValue(value: CharSequence?) {
        binding.valueText.text = value ?: ""
    }

    private fun setValueColor(color: Int) {
        binding.valueText.setTextColor(color)
    }

    private fun showChevron(show: Boolean) {
        binding.ivChevron.toggle(show)
    }

    private fun showSwitch(show: Boolean) {
        binding.commonSwitch.toggle(show)
    }

    fun setSwitchState(swithed: Boolean) {
        binding.commonSwitch.isChecked = swithed
    }
}
