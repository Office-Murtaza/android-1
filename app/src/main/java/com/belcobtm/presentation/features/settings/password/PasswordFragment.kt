package com.belcobtm.presentation.features.settings.password

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.navArgs
import com.belcobtm.R
import com.belcobtm.databinding.FragmentPasswordBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordFragment : BaseFragment<FragmentPasswordBinding>() {

    private val viewModel by viewModel<PasswordViewModel>()

    private val args: PasswordFragmentArgs by navArgs()

    private var appliedState: LoadingData<PasswordState>? = null
    override val retryListener = View.OnClickListener {
        viewModel.onNextClick(binding.passwordView.text?.toString().orEmpty())
    }

    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    override fun FragmentPasswordBinding.initViews() {
        appliedState = null
        setToolbarTitle(args.title)
        viewModel.passArgs(args)
    }

    override fun FragmentPasswordBinding.initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick(passwordView.text?.toString().orEmpty())
        }
        passwordView.addTextChangedListener {
            viewModel.onTextChanged(it?.toString().orEmpty())
        }
    }

    override fun FragmentPasswordBinding.initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                    passwordContainerView.isErrorEnabled = false
                }
                state.isButtonEnabled.doIfChanged(appliedState?.commonData?.isButtonEnabled) {
                    nextButton.isEnabled = it
                }
            },
            error = {
                showContent()
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    else -> {
                        passwordContainerView.isErrorEnabled = true
                        passwordContainerView.error = getString(R.string.password_doesnt_match)
                    }
                }
            },
            onUpdate = {
                appliedState = it
            }
        )
        viewModel.actionData.observe(viewLifecycleOwner) { action ->
            when (action) {
                is PasswordAction.NavigateAction -> {
                    showContent()
                    navigate(action.navDirections)
                }
                is PasswordAction.BackStackAction -> {
                    showContent()
                    popBackStack()
                }
                PasswordAction.PopToSecurityAction -> popBackStack(R.id.security_fragment, false)
            }
        }
    }

    override fun popBackStack(): Boolean {
        viewModel.popBackStack()
        return true
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPasswordBinding =
        FragmentPasswordBinding.inflate(inflater, container, false)
}