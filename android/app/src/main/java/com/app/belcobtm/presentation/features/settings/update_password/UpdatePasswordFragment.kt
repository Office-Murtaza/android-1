package com.app.belcobtm.presentation.features.settings.update_password

import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_update_password.*
import org.koin.android.viewmodel.ext.android.viewModel

class UpdatePasswordFragment : BaseFragment() {
    val viewModel by viewModel<UpdatePasswordViewModel>()
    private var appliedState: LoadingData<UpdatePasswordState>? = null

    override val resourceLayout = R.layout.fragment_update_password
    override val isHomeButtonEnabled = true

    override fun initViews() {
        setToolbarTitle(R.string.update_pass_label)
    }

    override fun initListeners() {
        oldPasswordView.addTextChangedListener {
            viewModel.onOldPassTextChanged(it.toString())
        }
        newPasswordView.addTextChangedListener {
            viewModel.onNewPassTextChanged(it.toString())
        }
        newPasswordConfirmView.addTextChangedListener {
            viewModel.onNewPassConfirmTextChanged(it.toString())
        }
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
    }

    override fun initObservers() {
        viewModel.stateData.observe(this) {state ->
            when (state) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Error -> showError(R.string.error_something_went_wrong)
                is LoadingData.Success -> {
                    state.doIfChanged(appliedState) {
                        showContent()
                    }
                    state.data.isOldPasswordError.doIfChanged(appliedState?.commonData?.isOldPasswordError, {
                        with (oldPasswordContainerView) {
                            isErrorEnabled = it
                            if (it) {
                                error = getString(R.string.password_doesnt_match)
                            }
                        }
                    })
                    state.data.isOldPasswordError.doIfChanged(appliedState?.commonData?.isOldPasswordError, {
                        with (oldPasswordContainerView) {
                            isErrorEnabled = it
                            if (it) {
                                error = getString(R.string.password_doesnt_match)
                            }
                        }
                    })
                    state.data.isNewPasswordMatches.doIfChanged(appliedState?.commonData?.isNewPasswordMatches, {
                        with (newPasswordConfirmContainerView) {
                            isErrorEnabled = !it
                            if (!it) {
                                error = getString(R.string.password_confirm_not_match)
                            }
                        }
                    })
                    state.data.isNextButtonEnabled.doIfChanged(appliedState?.commonData?.isNextButtonEnabled, {
                        nextButton.isEnabled = it
                    })
                }
            }
        }
        viewModel.actionData.observe(this) {action ->
            when (action) {
                is UpdatePasswordAction.Success -> {
                    showSnackBar(R.string.password_changed)
                    navigate(R.id.update_to_settings_fragment)
                }
            }
        }
    }
}