package com.app.belcobtm.presentation.features.settings.update_password

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentUpdatePasswordBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel

class UpdatePasswordFragment : BaseFragment<FragmentUpdatePasswordBinding>() {
    val viewModel by viewModel<UpdatePasswordViewModel>()
    private var appliedState: LoadingData<UpdatePasswordState>? = null
    override val retryListener = View.OnClickListener {
        viewModel.onNextClick()
    }

    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    override fun FragmentUpdatePasswordBinding.initViews() {
        appliedState = null
        setToolbarTitle(R.string.update_pass_label)
    }

    override fun FragmentUpdatePasswordBinding.initListeners() {
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

    override fun FragmentUpdatePasswordBinding.initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                }
                state.isOldPasswordError.doIfChanged(appliedState?.commonData?.isOldPasswordError, {
                    with(oldPasswordContainerView) {
                        isErrorEnabled = it
                        if (it) {
                            error = getString(R.string.password_doesnt_match)
                        }
                    }
                })
                state.isNewPasswordMatches.doIfChanged(appliedState?.commonData?.isNewPasswordMatches, {
                    with (newPasswordConfirmContainerView) {
                        isErrorEnabled = !it
                        if (!it) {
                            error = getString(R.string.password_confirm_not_match)
                        }
                    }
                })
                state.isNextButtonEnabled.doIfChanged(appliedState?.commonData?.isNextButtonEnabled, {
                    nextButton.isEnabled = it
                })
            },
            error = {
                when ((it as? Failure.MessageError)?.code) {
                    ERROR_PASSWORDS_SAME -> {
                        newPasswordContainerView.isErrorEnabled = true
                        newPasswordContainerView.error = getString(R.string.same_password)
                    }
                    else -> baseErrorHandler(it)
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is UpdatePasswordAction.Success -> {
                    showSnackBar(R.string.password_changed)
                    popBackStack()
                }
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUpdatePasswordBinding =
        FragmentUpdatePasswordBinding.inflate(inflater, container, false)
}