package com.app.belcobtm.presentation.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.databinding.ViewCoinInputLayoutBinding
import com.app.belcobtm.presentation.core.extensions.invisible
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toggle

class CoinInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val textColorOrigin: Int
    private val textColorError = ContextCompat.getColor(context, R.color.colorError)
    private val binding = ViewCoinInputLayoutBinding.inflate(LayoutInflater.from(context), this)

    init {
        textColorOrigin = binding.coinInputEditText.textColors.defaultColor
        setErrorText(null, false)
        setPadding(
            paddingLeft,
            paddingTop + resources.getDimensionPixelOffset(R.dimen.margin_half),
            paddingRight,
            paddingBottom + resources.getDimensionPixelOffset(R.dimen.margin_x2)
        )
    }

    fun getEditText(): EditText = binding.coinInputEditText

    fun setOnCoinButtonClickListener(listener: OnClickListener) {
        binding.coinButton.setOnClickListener(listener)
    }

    fun setOnMaxClickListener(listener: OnClickListener) {
        binding.tvMax.setOnClickListener(listener)
    }

    fun setCoinData(
        coinName: CharSequence?,
        @DrawableRes coinImage: Int,
        showCoinArrow: Boolean = true
    ) {
        binding.tvCoinName.text = coinName
        binding.ivCoin.setImageResource(coinImage)
        binding.ivCoinArrow.toggle(showCoinArrow)
    }

    fun setHelperText(text: CharSequence?) {
        binding.tvHelperText.text = text
    }

    fun setMaxVisible(visible: Boolean) {
        if (visible) {
            binding.tvMax.isEnabled = true
            binding.tvMax.show()
        } else {
            binding.tvMax.isEnabled = false
            binding.tvMax.invisible()
        }
    }

    fun setHelperText2(charSequence: CharSequence?) {
        binding.tvHelperText2.text = charSequence
    }

    fun setErrorEnabled(enabled: Boolean) {
        binding.tvError.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    fun setHint(hint: String) {
        binding.coinInputLayout.hint = hint
    }

    fun setMaxButtonEnabled(enabled: Boolean) {
        binding.tvMax.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    fun setErrorText(text: CharSequence?, highlightAmount: Boolean) {
        if (text != null) {
            if (highlightAmount) {
                binding.coinInputEditText.setTextColor(textColorError)
            } else {
                binding.coinInputEditText.setTextColor(textColorOrigin)
            }
            binding.tvError.visibility = View.VISIBLE
        } else {
            binding.coinInputEditText.setTextColor(textColorOrigin)
            binding.tvError.visibility = View.INVISIBLE
        }
        binding.tvError.text = text
    }
}
