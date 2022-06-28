package com.belcobtm.presentation.features.settings.update_password

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.belcobtm.R
import com.belcobtm.databinding.FragmentUpdatePasswordBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

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
                state.isOldPasswordError.doIfChanged(appliedState?.commonData?.isOldPasswordError) {
                    with(oldPasswordContainerView) {
                        if (it) {
                            error = getString(R.string.password_doesnt_match)
                        }
                    }
                }
                state.isNewPasswordMatches.doIfChanged(appliedState?.commonData?.isNewPasswordMatches) {
                    with(newPasswordConfirmContainerView) {
                        if (!it) {
                            error = getString(R.string.password_confirm_not_match)
                        }
                    }
                }
                state.isNextButtonEnabled.doIfChanged(appliedState?.commonData?.isNextButtonEnabled) {
                    nextButton.isEnabled = it
                }
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