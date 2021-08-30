package com.belcobtm.presentation.features.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSplashBinding
import com.belcobtm.domain.authorization.AuthorizationStatus
import com.belcobtm.domain.authorization.interactor.AuthorizationStatusGetUseCase
import com.belcobtm.domain.authorization.interactor.ClearAppDataUseCase
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.pin.code.PinCodeFragment
import org.koin.android.ext.android.inject

class SplashFragment : BaseFragment<FragmentSplashBinding>() {

    override val isToolbarEnabled: Boolean
        get() = false

    private val authorizationStatusUseCase: AuthorizationStatusGetUseCase by inject()
    private val clearAppDataUseCase: ClearAppDataUseCase by inject()

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSplashBinding = FragmentSplashBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().window.setBackgroundDrawableResource(R.drawable.bg_splash)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (authorizationStatusUseCase.invoke()) {
            AuthorizationStatus.PIN_CODE_CREATE -> showPinScreen(PinCodeFragment.KEY_PIN_MODE_CREATE)
            AuthorizationStatus.PIN_CODE_ENTER -> showPinScreen()
            else -> showAuthorizationScreen()
        }
    }

    override fun onDestroyView() {
        requireActivity().window.setBackgroundDrawableResource(R.drawable.default_bg)
        super.onDestroyView()
    }

    private fun showPinScreen(mode: String = PinCodeFragment.KEY_PIN_MODE_ENTER) {
        navigate(
            R.id.nav_pin_code,
            bundleOf(PinCodeFragment.TAG_PIN_MODE to mode),
            NavOptions.Builder()
                .setPopUpTo(R.id.splash_screen, true)
                .build()
        )
    }

    private fun showAuthorizationScreen() {
        clearAppDataUseCase.invoke()
        navigate(
            R.id.nav_authorization,
            NavOptions.Builder()
                .setPopUpTo(R.id.splash_screen, true)
                .build()
        )
    }
}