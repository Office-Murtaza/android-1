package com.app.belcobtm.presentation.features.pin.code

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Observer
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import kotlinx.android.synthetic.main.fragment_pin_code.*
import org.koin.android.viewmodel.ext.android.viewModel

class PinCodeFragment : BaseFragment() {
    private val viewModel: PinCodeViewModel by viewModel()
    private val pinMode: String by lazy { requireArguments().getString(TAG_PIN_MODE, KEY_PIN_MODE_ENTER) }
    override val resourceLayout: Int = R.layout.fragment_pin_code
    override val isToolbarEnabled: Boolean = false
    override val backPressedListener: View.OnClickListener = View.OnClickListener { /**empty**/ }

    override fun initViews() {
        refreshTitle()
    }

    override fun initListeners() {
        key1View.setOnClickListener { addPinSymbol(it) }
        key2View.setOnClickListener { addPinSymbol(it) }
        key3View.setOnClickListener { addPinSymbol(it) }
        key4View.setOnClickListener { addPinSymbol(it) }
        key5View.setOnClickListener { addPinSymbol(it) }
        key6View.setOnClickListener { addPinSymbol(it) }
        key7View.setOnClickListener { addPinSymbol(it) }
        key8View.setOnClickListener { addPinSymbol(it) }
        key9View.setOnClickListener { addPinSymbol(it) }
        key0View.setOnClickListener { addPinSymbol(it) }
        keyEraseView.setOnClickListener { removePinSymbol() }
    }

    override fun initObservers() {
        viewModel.authorizationLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    (requireActivity() as HostActivity).showMainScreen()
                    showContent()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSnackBar(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> (requireActivity() as HostActivity).showAuthorizationScreen()
                    }
                    showContent()
                }
            }
        })
    }

    private fun addPinSymbol(view: View) {
        val createdPin = viewModel.enteredCreatePinCode
        val pin = viewModel.enteredPinCode
        val selectedKey = view.tag as String
        when {
            pinMode != KEY_PIN_MODE_ENTER
                    && pin.length == PIN_CODE_LENGTH
                    && createdPin.length < PIN_CODE_LENGTH -> viewModel.enteredCreatePinCode += selectedKey
            pin.length < PIN_CODE_LENGTH -> viewModel.enteredPinCode += selectedKey
        }
        onPinChange()
    }

    private fun removePinSymbol() {
        val createdPin = viewModel.enteredCreatePinCode
        val pin = viewModel.enteredPinCode
        when {
            pinMode != KEY_PIN_MODE_ENTER
                    && pin.length == PIN_CODE_LENGTH
                    && createdPin.length <= PIN_CODE_LENGTH -> viewModel.enteredCreatePinCode = createdPin.dropLast(1)
            pin.length < PIN_CODE_LENGTH -> viewModel.enteredPinCode = pin.dropLast(1)
        }
        onPinChange()
    }


    private fun unselectTintDot(tintGrayDot: AppCompatImageView) = ImageViewCompat.setImageTintList(
        tintGrayDot,
        ContextCompat.getColorStateList(requireContext(), R.color.colorPinUnselectedIndicator)
    )

    private fun selectTintDot(tintDot: AppCompatImageView, whiteDot: AppCompatImageView? = null) {
        ImageViewCompat.setImageTintList(
            tintDot,
            ContextCompat.getColorStateList(requireContext(), R.color.colorPinSelectedIndicator)
        )
        if (whiteDot != null) {
            ImageViewCompat.setImageTintList(
                whiteDot,
                ContextCompat.getColorStateList(requireContext(), R.color.colorPinUnselectedIndicator)
            )
        }
    }

    private fun clearIndicators() {
        unselectTintDot(dot1View)
        unselectTintDot(dot2View)
        unselectTintDot(dot3View)
        unselectTintDot(dot4View)
        unselectTintDot(dot5View)
        unselectTintDot(dot6View)
    }

    private fun onPinChange() {
        val pinCode = when {
            pinMode != KEY_PIN_MODE_ENTER && viewModel.enteredPinCode.length == PIN_CODE_LENGTH -> viewModel.enteredCreatePinCode
            else -> viewModel.enteredPinCode
        }

        when {
            pinCode.isEmpty() -> clearIndicators()
            pinCode.length == 1 -> selectTintDot(dot1View, dot2View)
            pinCode.length == 2 -> selectTintDot(dot2View, dot3View)
            pinCode.length == 3 -> selectTintDot(dot3View, dot4View)
            pinCode.length == 4 -> selectTintDot(dot4View, dot5View)
            pinCode.length == 5 -> selectTintDot(dot5View, dot6View)
            pinCode.length == 6 -> {
                selectTintDot(dot6View)
                onPinEntered(pinCode)
            }
            else -> Unit
        }
        refreshTitle()
    }

    private fun onPinEntered(pinCode: String) {
        when {
            pinMode != KEY_PIN_MODE_ENTER && viewModel.enteredCreatePinCode.isBlank() -> {//first create/change pin screen
                vibrate(100)
                clearIndicators()
            }
            pinMode != KEY_PIN_MODE_ENTER && viewModel.enteredCreatePinCode == viewModel.enteredPinCode -> {
                vibrate(300)
                viewModel.savePinCode(viewModel.enteredPinCode)
                when (pinMode) {
                    KEY_PIN_MODE_CHANGE -> showSnackBar(R.string.code_changed)
                    KEY_PIN_MODE_CREATE -> showSnackBar(R.string.code_created)
                }
                (requireActivity() as HostActivity).showMainScreen()
            }
            pinMode == KEY_PIN_MODE_ENTER && pinCode == viewModel.getSavedPinCode() -> {
                vibrate(50)
                viewModel.authorize()
            }
            pinMode == KEY_PIN_MODE_ENTER && pinCode != viewModel.getSavedPinCode() -> {
                viewModel.enteredPinCode = ""
                showSnackBar(R.string.code_not_match)
                vibrateError()
                clearIndicators()
            }
            else -> {//second create/change pin screen. pin doesn't phone
                viewModel.enteredPinCode = ""
                viewModel.enteredCreatePinCode = ""
                showSnackBar(R.string.code_not_match)
                clearIndicators()
            }
        }
    }

    private fun refreshTitle() {
        when {
            pinMode != KEY_PIN_MODE_ENTER && viewModel.enteredPinCode.length < PIN_CODE_LENGTH ->
                titleView.setText(R.string.setup_pin_code)
            pinMode != KEY_PIN_MODE_ENTER && viewModel.enteredPinCode.length == PIN_CODE_LENGTH ->
                titleView.setText(R.string.pin_code_screen_confirm_pin)
            pinMode == KEY_PIN_MODE_ENTER && viewModel.enteredPinCode.length < PIN_CODE_LENGTH ->
                titleView.setText(R.string.pin_code_screen_enter_pin)
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
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
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

    companion object {
        const val TAG_PIN_MODE = "tag_pin_mode"
        const val KEY_PIN_MODE_CREATE = "key_pin_mode_create"
        const val KEY_PIN_MODE_CHANGE = "key_pin_mode_change"
        const val KEY_PIN_MODE_ENTER = "key_pin_mode_enter"

        const val PIN_CODE_LENGTH: Int = 6
    }
}