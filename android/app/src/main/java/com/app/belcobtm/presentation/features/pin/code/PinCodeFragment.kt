package com.app.belcobtm.presentation.features.pin.code

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.lifecycle.observe
import com.app.belcobtm.App
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.toggle
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.HostActivity
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import kotlinx.android.synthetic.main.fragment_pin_code.*
import org.koin.android.viewmodel.ext.android.viewModel

class PinCodeFragment : BaseFragment() {
    private val viewModel: PinCodeViewModel by viewModel()
    private val pinMode: String by lazy {
        requireArguments().getString(
            TAG_PIN_MODE,
            KEY_PIN_MODE_ENTER
        )
    }
    override val resourceLayout: Int = R.layout.fragment_pin_code
    override val isToolbarEnabled: Boolean = false
    override val backPressedListener: View.OnClickListener = View.OnClickListener {
        if (pinMode == KEY_PIN_MODE_CHANGE) {
            navigate(PinCodeFragmentDirections.pinCodeToSettingsFragment(SETTINGS_SECURITY))
        } else {
            //do nothing, user need to enter/create pin
        }
    }
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.authorize() }

    private var appliedState: PinCodeState? = null

    override fun initViews() {
        appliedState = null
        viewModel.setMode(pinMode)
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
        backButtonView.setOnClickListener {
            viewModel.onBackClick()
        }
    }

    override fun initObservers() {
        viewModel.stateData.observe(this) { state ->
            state.isLoading.doIfChanged(appliedState?.isLoading) {
                if (it) {
                    showLoading()
                } else {
                    showContent()
                }
            }
            state.visiblePin.doIfChanged(appliedState?.visiblePin) {
                pinIndicatorsView.setText(it)
            }
            state.labelResource.doIfChanged(appliedState?.labelResource) {
                titleView.setText(it)
            }
            state.isError.doIfChanged(appliedState?.isError) {
                errorView.toggle(it)
                if (it) {
                    vibrateError()
                }
            }
            state.backButtonVisible.doIfChanged(appliedState?.backButtonVisible) {
                backButtonView.toggle(it)
            }
        }
        viewModel.actionData.observe(this) { action ->
            when (action) {
                is PinCodeAction.Success -> {
                    (requireActivity() as HostActivity).showMainScreen()
                }
                is PinCodeAction.ChangedPin -> {
                    showSnackBar(R.string.pin_updated)
                    navigate(PinCodeFragmentDirections.pinCodeToSettingsFragment(SETTINGS_SECURITY))
                }
                is PinCodeAction.Vibrate -> {
                    vibrate(action.duration)
                }
                is PinCodeAction.AuthorizeError -> {
                    showErrorServerError()
                }
            }
        }
    }

    private fun addPinSymbol(view: View) {
        viewModel.onAddPinSymbol(view.tag as String)
    }

    private fun removePinSymbol() {
        viewModel.onRemoveSymbol()
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

        const val STEP_VERIFY = 0
        const val STEP_CREATE = 1
        const val STEP_CONFIRM = 2

        const val PIN_CODE_LENGTH: Int = 6
    }
}