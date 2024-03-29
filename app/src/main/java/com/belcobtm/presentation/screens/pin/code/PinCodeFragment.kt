package com.belcobtm.presentation.screens.pin.code

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import androidx.activity.OnBackPressedCallback
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.belcobtm.App
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPinCodeBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.tools.extensions.toggle
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.HostActivity
import com.belcobtm.presentation.screens.MainFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PinCodeFragment : BaseFragment<FragmentPinCodeBinding>() {

    private val viewModel: PinCodeViewModel by viewModel()

    private val pinMode: String by lazy {
        requireArguments().getString(
            TAG_PIN_MODE,
            KEY_PIN_MODE_ENTER
        )
    }
    override val isToolbarEnabled: Boolean = false
    override var isMenuEnabled: Boolean = true
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.authorize() }

    private var appliedState: PinCodeState? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (pinMode == KEY_PIN_MODE_CHANGE) {
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        return root
    }

    override fun FragmentPinCodeBinding.initViews() {
        appliedState = null
        pinIndicatorsView.setMaxLength(PIN_CODE_LENGTH)
        viewModel.setMode(pinMode)
    }

    override fun FragmentPinCodeBinding.initListeners() {
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
        keyBioView.setOnClickListener {
            // since the visibility could not be set to GONE
            // we have to check first if this button is visible to a user
            if (viewModel.bioAuthVisible.value == true) startBioPromt()
        }
        backButtonView.setOnClickListener {
            viewModel.onBackClick()
        }
    }

    override fun FragmentPinCodeBinding.initObservers() {
        viewModel.stateData.observe(viewLifecycleOwner) { state ->
            state.isLoading.doIfChanged(appliedState?.isLoading) {
                if (it) {
                    showLoading()
                } else {
                    showContent()
                }
            }
            state.showMenu.doIfChanged(appliedState?.showMenu) {
                if (it) {
                    keyboardView.layoutParams = keyboardView.layoutParams.apply {
                        height = TableLayout.LayoutParams.WRAP_CONTENT
                    }
                }
            }
            state.visiblePin.doIfChanged(appliedState?.visiblePin) {
                binding.pinIndicatorsView.setText(it)
            }
            state.labelResource.doIfChanged(appliedState?.labelResource) {
                binding.titleView.setText(it)
            }
            state.isError.doIfChanged(appliedState?.isError) {
                binding.errorView.toggle(it)
                if (it) {
                    vibrateError()
                }
            }
            state.backButtonVisible.doIfChanged(appliedState?.backButtonVisible) {
                binding.backButtonView.toggle(it)
            }
        }
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is PinCodeAction.Success -> {
                    val deeplink = arguments?.getString(KEY_DEEPLINK)
                    viewModel.saveUserAuthed(true)
                    if (deeplink.isNullOrEmpty()) {
                        navigate(
                            R.id.main_screen,
                            NavOptions.Builder()
                                .setPopUpTo(R.id.pin_code_fragment, true)
                                .build()
                        )
                    } else {
                        navigate(
                            R.id.main_screen,
                            bundleOf(MainFragment.KEY_DEEPLINK to deeplink),
                            NavOptions.Builder()
                                .setPopUpTo(R.id.pin_code_fragment, true)
                                .build()
                        )
                    }
                }
                is PinCodeAction.ChangedPin -> {
                    showToast(R.string.pin_updated)
                    popBackStack(R.id.security_fragment, false)
                }
                is PinCodeAction.Vibrate -> vibrate(action.duration)
                is PinCodeAction.StartWelcomeScreen -> {
                    (requireActivity() as HostActivity).showAuthorizationScreen()
                }
                is PinCodeAction.AuthorizeError -> when (action.failure) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showToast(action.failure.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> showErrorServerError()
                    else -> showErrorSomethingWrong()
                }
                is PinCodeAction.BackPress -> popBackStack(R.id.security_fragment, false)
                PinCodeAction.StartBioPromt -> startBioPromt()
            }
        }
        viewModel.bioAuthVisible.observe(viewLifecycleOwner) { visible ->
            binding.keyBioView.isInvisible = !visible
        }
    }

    private fun startBioPromt() {
        val mainExecutor = ContextCompat.getMainExecutor(requireContext())
        val promtInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.pin_code_screen_bio_title))
            .setNegativeButtonText(getString(R.string.pin_code_screen_bio_cancel))
            .build()
        val promtCallback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                viewModel.authorize()
            }
        }
        BiometricPrompt(this, mainExecutor, promtCallback).authenticate(promtInfo)
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
        const val KEY_DEEPLINK = "key_deeplink"

        const val STEP_VERIFY = 0
        const val STEP_CREATE = 1
        const val STEP_CONFIRM = 2

        const val PIN_CODE_LENGTH: Int = 4
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPinCodeBinding =
        FragmentPinCodeBinding.inflate(inflater, container, false)

}
