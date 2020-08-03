package com.app.belcobtm.presentation.features.settings.phone

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import kotlinx.android.synthetic.main.fragment_change_phone.*
import kotlinx.android.synthetic.main.fragment_display_phone.nextButton
import org.koin.android.viewmodel.ext.android.viewModel

class PhoneChangeFragment: BaseFragment() {
    val viewModel by viewModel<PhoneChangeViewModel>()
    private var appliedState: PhoneChangeState? = null
    override val resourceLayout = R.layout.fragment_change_phone
    override val isHomeButtonEnabled = true

    override fun initViews() {
        setToolbarTitle(R.string.update_phone_label)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
        phoneView.addTextChangedListener {
            viewModel.onPhoneInput(it?.toString().orEmpty())
        }
    }

    override fun popBackStack(): Boolean {
        getNavController()?.navigate(PhoneChangeFragmentDirections.changePhoneToSettings(SETTINGS_SECURITY))
        return true
    }

    override fun initObservers() {
        viewModel.stateData.observe(this) { state ->
            when (state) {
                is PhoneChangeState.Loading -> showLoading()
                is PhoneChangeState.Error -> showError(R.string.error_something_went_wrong)
                is PhoneChangeState.Ready -> {
                    state.doIfChanged(appliedState) {
                        showContent()
                    }
                    state.isNextButtonEnabled.doIfChanged((appliedState as? PhoneChangeState.Ready)?.isNextButtonEnabled) {
                        nextButton.isEnabled = it
                    }
                }
            }
        }
        viewModel.actionData.observe(this) { action ->
            when (action) {
                is PhoneChangeAction.NavigateAction -> navigate(action.navDirections)
            }
        }
    }
}