package com.app.belcobtm.presentation.features.authorization.pin

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.onTextChanged
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.presentation.features.authorization.welcome.WelcomeActivity
import kotlinx.android.synthetic.main.activity_pin.*
import org.jetbrains.anko.toast
import org.koin.android.viewmodel.ext.android.viewModel


class PinActivity : BaseActivity() {
    private val viewModel: PinViewModel by viewModel()
    private lateinit var mMode: Mode

    private var mPin1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        initViews()
        initListeners()
        initObservers()
    }

    override fun onBackPressed() = if (
        (mMode == Mode.MODE_CREATE_PIN || mMode == Mode.MODE_CHANGE_PIN) && mPin1 != null
    ) {
        iconTextView.setText(R.string.setup_pin_code)
        mPin1 = null
        codeView.setText("")
    } else {
        finishAffinity()
        finish()
    }

    private fun initViews() {
        mMode = Mode.valueOfInt(intent.getIntExtra(KEY_MODE, Mode.MODE_PIN.ordinal))

        if (mMode == Mode.MODE_PIN) {
            iconTextView.text = getString(R.string.enter_pin_code)
        }
    }

    private fun initListeners() {
        key1View.setOnClickListener { addPinSymbol('1') }
        key2View.setOnClickListener { addPinSymbol('2') }
        key3View.setOnClickListener { addPinSymbol('3') }
        key4View.setOnClickListener { addPinSymbol('4') }
        key5View.setOnClickListener { addPinSymbol('5') }
        key6View.setOnClickListener { addPinSymbol('6') }
        key7View.setOnClickListener { addPinSymbol('7') }
        key8View.setOnClickListener { addPinSymbol('8') }
        key9View.setOnClickListener { addPinSymbol('9') }
        key0View.setOnClickListener { addPinSymbol('0') }
        keyEraseView.setOnClickListener {
            val codeStr = codeView.text.toString()
            if (codeStr.isNotEmpty())
                codeView.setText(codeStr.substring(0, codeStr.length - 1))
        }
        codeView.onTextChanged { onPinChange(it) }
    }

    private fun initObservers() {
        viewModel.authorizationLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress(true)
                is LoadingData.Success -> {
                    setResult(Activity.RESULT_OK, Intent())
                    showProgress(false)
                    finish()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.TokenError -> {
                            finishAffinity()
                            startActivity(Intent(this, WelcomeActivity::class.java))
                        }
                        is Failure.MessageError -> showError(it.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    showProgress(false)
                }
            }
        })
    }

    private fun onPinChange(pin: String) = when {
        pin.isEmpty() -> {
            tintGrayDot(dot1View)
            tintGrayDot(dot2View)
            tintGrayDot(dot3View)
            tintGrayDot(dot4View)
            tintGrayDot(dot5View)
            tintGrayDot(dot6View)
        }
        pin.length == 1 -> tintDots(dot1View, dot2View)
        pin.length == 2 -> tintDots(dot2View, dot3View)
        pin.length == 3 -> tintDots(dot3View, dot4View)
        pin.length == 4 -> tintDots(dot4View, dot5View)
        pin.length == 5 -> tintDots(dot5View, dot6View)
        pin.length == 6 -> {
            tintDots(dot6View)
            onPinEntered(pin)
        }
        else -> Unit
    }

    private fun tintDots(tintDot: AppCompatImageView, whiteDot: AppCompatImageView? = null) {
        ImageViewCompat.setImageTintList(tintDot, ContextCompat.getColorStateList(iconView.context, R.color.bt_orange))
        if (whiteDot != null) {
            ImageViewCompat.setImageTintList(
                whiteDot,
                ContextCompat.getColorStateList(iconView.context, R.color.pin_gray_unchecked)
            )
        }
    }

    private fun tintGrayDot(tintGrayDot: AppCompatImageView) = ImageViewCompat.setImageTintList(
        tintGrayDot,
        ContextCompat.getColorStateList(iconView.context, R.color.pin_gray_unchecked)
    )

    private fun onPinEntered(pinCode: String) = when {
        mMode == Mode.MODE_PIN -> if (pinCode == viewModel.getSavedPinCode()) {
            vibrate(50)
            viewModel.authorize()
        } else {
            pinNotMatch()
        }
        mPin1 == null -> {//first create/change pin screen
            mPin1 = codeView.text.toString()
            iconTextView.setText(R.string.confirm_pin_code)
            codeView.setText("")
            vibrate(100)
        }
        mPin1 == codeView.text.toString() -> {//second create/change pin screen. pin matches
            viewModel.setPinCode(mPin1 ?: "")
            if (mMode == Mode.MODE_CHANGE_PIN) {
                toast(R.string.code_changed)
            } else if (mMode == Mode.MODE_CREATE_PIN) {
                toast(R.string.code_created)
            }
            vibrate(300)
            setResult(Activity.RESULT_OK)
            finish()
        }
        else -> {//second create/change pin screen. pin doesn't phone
            pinNotMatch()
            onBackPressed()
        }
    }

    private fun addPinSymbol(char: Char) {
        if (codeView.text.toString().length < 6) {
            codeView.text?.append(char)
        }
    }

    private fun vibrateError() {
        val pattern = longArrayOf(0, 55, 55, 55)
        val vibrator = App.appContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amplitudes = IntArray(pattern.size)
                for (i in 0 until pattern.size / 2) {
                    amplitudes[i * 2 + 1] = 170
                }
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        pattern,
                        amplitudes,
                        -1
                    )
                )
            } else {
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    private fun vibrate(milliseconds: Long) {
        val vibrator = App.appContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) { // Vibrator availability checking
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, 100))
            } else {
                vibrator.vibrate(milliseconds)
            }
        }
    }

    private fun pinNotMatch() {
        showError(R.string.code_not_match)
        vibrateError()
        codeView.setText("")
    }

    companion object {
        private const val KEY_MODE = "KEY_MODE"

        fun getIntent(context: Context, mode: Mode): Intent = Intent(context, PinActivity::class.java).also {
            it.putExtra(KEY_MODE, mode.ordinal)
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
}
