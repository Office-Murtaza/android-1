package com.app.belcobtm.ui.auth.pin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.welcome.WelcomeActivity
import com.app.belcobtm.util.MyCustomTextWatcher
import kotlinx.android.synthetic.main.activity_pin.*
import org.jetbrains.anko.toast


class PinActivity : BaseMvpActivity<PinContract.View, PinContract.Presenter>(), PinContract.View {

    private lateinit var mMode: Mode

    private var mPin1: String? = null

    companion object {
        private val KEY_MODE = "KEY_MODE"

        fun getIntent(context: Context, mode: Mode): Intent {
            val intent = Intent(context, PinActivity::class.java)
            intent.putExtra(KEY_MODE, mode.ordinal)
            return intent
        }

        enum class Mode {
            MODE_CREATE_PIN,
            MODE_CHANGE_PIN,
            MODE_PIN;

            companion object {
                fun valueOfInt(index: Int): Mode {
                    return values()[index]
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        mMode = Mode.valueOfInt(intent.getIntExtra(KEY_MODE, Mode.MODE_PIN.ordinal))

        num_1.setOnClickListener { addPinSymbol('1') }
        num_2.setOnClickListener { addPinSymbol('2') }
        num_3.setOnClickListener { addPinSymbol('3') }
        num_4.setOnClickListener { addPinSymbol('4') }
        num_5.setOnClickListener { addPinSymbol('5') }
        num_6.setOnClickListener { addPinSymbol('6') }
        num_7.setOnClickListener { addPinSymbol('7') }
        num_8.setOnClickListener { addPinSymbol('8') }
        num_9.setOnClickListener { addPinSymbol('9') }
        num_0.setOnClickListener { addPinSymbol('0') }

        erase.setOnClickListener {
            val codeStr = code.text.toString()
            if (codeStr.isNotEmpty())
                code.setText(codeStr.substring(0, codeStr.length - 1))
        }

        code.addTextChangedListener(object : MyCustomTextWatcher() {
            override fun afterTextChanged(pin: Editable) {
                onPinChange(pin.toString())
            }
        })

        if (mMode == Mode.MODE_PIN) {
            icon_text.text = getString(R.string.enter_pin_code)
        }
    }


    private fun onPinChange(pin: String) {
        when {
            pin.isEmpty() -> {
                tintGrayDot(dot_1)
                tintGrayDot(dot_2)
                tintGrayDot(dot_3)
                tintGrayDot(dot_4)
                tintGrayDot(dot_5)
                tintGrayDot(dot_6)
            }
            pin.length == 1 -> tintDots(dot_1, dot_2)
            pin.length == 2 -> tintDots(dot_2, dot_3)
            pin.length == 3 -> tintDots(dot_3, dot_4)
            pin.length == 4 -> tintDots(dot_4, dot_5)
            pin.length == 5 -> tintDots(dot_5, dot_6)
            pin.length == 6 -> {
                tintDots(dot_6)
                onPinEntered(pin)
            }
        }
    }

    private fun tintDots(tintDot: AppCompatImageView, whiteDot: AppCompatImageView? = null) {
        ImageViewCompat.setImageTintList(
            tintDot,
            ColorStateList.valueOf(resources.getColor(R.color.bt_orange))
        )
        if (whiteDot != null) ImageViewCompat.setImageTintList(
            whiteDot,
            ColorStateList.valueOf(resources.getColor(R.color.pin_gray_unchecked))
        )
    }

    private fun tintGrayDot(tintGrayDot: AppCompatImageView) {
        ImageViewCompat.setImageTintList(
            tintGrayDot,
            ColorStateList.valueOf(resources.getColor(R.color.pin_gray_unchecked))
        )
    }

    private fun onPinEntered(pin: String) {
        if (mMode == Mode.MODE_PIN) {
            mPresenter.checkCryptoPin(pin)
        } else {
            when (mPin1) {
                null -> {//first create/change pin screen
                    mPin1 = code.text.toString()
                    icon_text.text = "Confirm PIN Code"//todo move text to toolbar
                    code.setText("")
                    mPresenter.vibrate(100)
                }
                code.text.toString() -> {//second create/change pin screen. pin matches
                    mPresenter.savePin(mPin1!!)
                    if (mMode == Mode.MODE_CHANGE_PIN) {
                        toast(R.string.code_changed)
                    } else if (mMode == Mode.MODE_CREATE_PIN) {
                        toast(R.string.code_created)
                    }
                    mPresenter.vibrate(300)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                else -> {//second create/change pin screen. pin doesn't phone
                    pinNotMatch()
                    onBackPressed()
                }
            }
        }
    }

    override fun pinNotMatch() {
        showError(R.string.code_not_match)
        mPresenter.vibrateError()
        code.setText("")
    }

    override fun onBackPressed() {
        if ((mMode == Mode.MODE_CREATE_PIN || mMode == Mode.MODE_CHANGE_PIN) && mPin1 != null) {
            icon_text.text = "Setup PIN Code"//todo move text to toolbar
            mPin1 = null
            code.setText("")
        } else {
            finishAffinity()
            finish()
        }
    }

    private fun addPinSymbol(char: Char) {
        if (code.text.toString().length < 6)
            code.text?.append(char)
    }

    override fun closeScreenAndContinue() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onRefreshTokenFailed() {
        finishAffinity()
        startActivity(Intent(this, WelcomeActivity::class.java))
    }
}
