package com.app.belcobtm.presentation.features.settings.password

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.settings.SettingsFragment.Companion.SETTINGS_SECURITY
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_password.nextButton
import org.koin.android.viewmodel.ext.android.viewModel

class PasswordFragment : BaseFragment() {
    private val viewModel by viewModel<PasswordViewModel>()
    private val args:  PasswordFragmentArgs by navArgs()
    private var appliedState: LoadingData<PasswordState>? = null
    override val retryListener = View.OnClickListener {
        viewModel.onNextClick(passwordView.text?.toString().orEmpty())
    }

    override val resourceLayout: Int = R.layout.layout_password
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true

    override fun initViews() {
        appliedState = null
        setToolbarTitle(args.title)
        viewModel.passArgs(args)
    }

    override fun initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick(passwordView.text?.toString().orEmpty())
        }
        passwordView.addTextChangedListener {
            viewModel.onTextChanged(it?.toString().orEmpty())
        }
    }

    override fun initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState, {
                    showContent()
                    passwordContainerView.isErrorEnabled = false
                })
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
        viewModel.actionData.observe(this, Observer { action ->
            when (action) {
                is PasswordAction.NavigateAction -> {
                    showContent()
                    navigate(action.navDirections)
                }
                is PasswordAction.BackStackAction -> {
                    showContent()
                    getNavController()?.popBackStack()
                }
            }
        })
    }

    override fun popBackStack(): Boolean {
        viewModel.popBackStack()
        return true
    }
}