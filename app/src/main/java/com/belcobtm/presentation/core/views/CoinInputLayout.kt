package com.belcobtm.presentation.core.views

import android.content.Context
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.belcobtm.R
import com.belcobtm.databinding.ViewCoinInputLayoutBinding
import com.belcobtm.presentation.tools.extensions.actionDoneListener
import com.belcobtm.presentation.tools.extensions.setDrawableEnd
import com.belcobtm.presentation.tools.extensions.toggle

class CoinInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val textColorOrigin: Int
    private val textColorError = ContextCompat.getColor(context, R.color.colorError)
    private val binding = ViewCoinInputLayoutBinding.inflate(LayoutInflater.from(context), this)

    init {
        textColorOrigin = binding.coinInputEditText.textColors.defaultColor
        setErrorText(null, false)
        binding.coinInputEditText.actionDoneListener {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            val activity = context as AppCompatActivity
            imm?.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            binding.coinInputEditText.clearFocus()
        }
    }

    fun getEditText(): EditText = binding.coinInputEditText

    fun updateInputMode(isIntOnly: Boolean = false) {
        binding.coinInputEditText.inputType = if (isIntOnly)
            InputType.TYPE_CLASS_NUMBER
        else
            InputType.TYPE_NUMBER_FLAG_DECIMAL
    }

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

    fun setHelperTextWithLink(text: CharSequence, clickablePart: String, listener: () -> Unit) {
        val ss = SpannableString(text)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                listener()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        val start = text.indexOf(clickablePart)
        ss.setSpan(
            clickableSpan,
            start,
            start + clickablePart.length,
            Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        )
        binding.tvHelperText.setDrawableEnd(R.drawable.ic_chevron_left_link)
        binding.tvHelperText.movementMethod = LinkMovementMethod.getInstance()
        binding.tvHelperText.text = ss
    }

    fun setMaxVisible(visible: Boolean) {
        binding.tvMax.isEnabled = visible
        binding.tvMax.isInvisible = visible.not()
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
