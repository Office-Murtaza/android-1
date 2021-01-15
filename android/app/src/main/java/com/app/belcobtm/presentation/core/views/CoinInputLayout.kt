package com.app.belcobtm.presentation.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.invisible
import com.app.belcobtm.presentation.core.extensions.show
import com.app.belcobtm.presentation.core.extensions.toggle

class CoinInputLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val textColorOrigin: Int
    private val textColorError = ContextCompat.getColor(context, R.color.colorError)
    private val tvMax: TextView
    private val coinButton: View
    private val tvError: TextView
    private val ivCoin: ImageView
    private val ivCoinArrow: ImageView
    private val editText: EditText
    private val tvCoinName: TextView
    private val tvHelperText: TextView
    private val tvAdditionalHelperText: TextView
    private val tvHelperText2: TextView

    init {
        inflate(context, R.layout.view_coin_input_layout, this)
        tvMax = findViewById(R.id.tvMax)
        ivCoin = findViewById(R.id.ivCoin)
        ivCoinArrow = findViewById(R.id.ivCoinArrow)
        tvError = findViewById(R.id.tvError)
        tvCoinName = findViewById(R.id.tvCoinName)
        coinButton = findViewById(R.id.coinButton)
        tvHelperText = findViewById(R.id.tvHelperText)
        tvAdditionalHelperText = findViewById(R.id.tvAdditionalHelperText)
        tvHelperText2 = findViewById(R.id.tvHelperText2)
        editText = findViewById(R.id.coinInputEditText)

        textColorOrigin = editText.textColors.defaultColor
        setErrorText(null)
    }

    fun getEditText(): EditText = editText

    fun setOnCoinButtonClickListener(listener: OnClickListener) {
        coinButton.setOnClickListener(listener)
    }

    fun setOnMaxClickListener(listener: OnClickListener) {
        tvMax.setOnClickListener(listener)
    }

    fun setCoinData(
        coinName: CharSequence?,
        @DrawableRes coinImage: Int,
        showCoinArrow: Boolean = true
    ) {
        tvCoinName.text = coinName
        ivCoin.setImageResource(coinImage)
        ivCoinArrow.toggle(showCoinArrow)
    }

    fun setHelperText(text: CharSequence?) {
        tvHelperText.text = text
    }

    fun setAdditionalHelperText(text: CharSequence?) {
        tvAdditionalHelperText.text = text
    }

    fun setMaxVisible(visible: Boolean) {
        if (visible) {
            tvMax.isEnabled = true
            tvMax.show()
        } else {
            tvMax.isEnabled = false
            tvMax.invisible()
        }
    }

    fun setHelperText2(charSequence: CharSequence?) {
        tvHelperText2.text = charSequence
    }

    fun setErrorText(text: CharSequence?) {
        if (text != null) {
            editText.setTextColor(textColorError)
            tvError.visibility = View.VISIBLE
        } else {
            editText.setTextColor(textColorOrigin)
            tvError.visibility = View.INVISIBLE
        }
        tvError.text = text
    }
}
