package com.app.belcobtm.presentation.features.settings.phone

import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import kotlinx.android.synthetic.main.fragment_display_phone.*
import org.koin.android.viewmodel.ext.android.viewModel

class PhoneDisplayFragment : BaseFragment() {
    val viewModel by viewModel<PhoneDisplayViewModel>()
    private var appliedState: PhoneDisplayState? = null
    override val resourceLayout = R.layout.fragment_display_phone

    override val isHomeButtonEnabled = true

    override fun initViews() {
        setToolbarTitle(R.string.update_phone_label)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            navigate(
                PhoneDisplayFragmentDirections.phoneDisplayToPasswordFragment(
                    R.id.password_to_change_phone_fragment,
                    R.string.update_phone_label
                )
            )
        }
    }

    override fun initObservers() {
        viewModel.stateData.observe(this) { state ->
            when (state) {
                is PhoneDisplayState.Loading -> showLoading()
                is PhoneDisplayState.Error -> showError(R.string.error_something_went_wrong)
                is PhoneDisplayState.Ready -> {
                    state.doIfChanged(appliedState) {
                        showContent()
                    }
                    state.phone.doIfChanged((appliedState as? PhoneDisplayState.Ready)?.phone) {
                        phoneNumber.text = it
                    }
                    state.isNextButtonEnabled.doIfChanged((appliedState as? PhoneDisplayState.Ready)?.isNextButtonEnabled) {
                        nextButton.isEnabled = it
                    }
                }
            }
        }
    }

    override fun popBackStack(): Boolean {
        getNavController()?.navigate(PhoneDisplayFragmentDirections.phoneDisplayToSettingsFragment(SETTINGS_SECURITY))
        return true
    }
}