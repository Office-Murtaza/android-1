package com.app.belcobtm.presentation.features.settings.password

import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.R
import kotlinx.android.synthetic.main.layout_password.*
import kotlinx.android.synthetic.main.layout_password.nextButton
import org.koin.android.viewmodel.ext.android.viewModel

class PasswordFragment : BaseFragment() {
    private val viewModel by viewModel<PasswordViewModel>()
    private val args:  PasswordFragmentArgs by navArgs()
    private val appliedState: PasswordState? = null

    override val resourceLayout: Int = R.layout.layout_password
    override val isHomeButtonEnabled = true

    override fun initViews() {
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
        viewModel.stateData.observe(this, Observer { state ->
            when (state) {
                is PasswordState.Loading -> showLoading()
                is PasswordState.Ready -> {
                    state.doIfChanged(appliedState, {
                        showContent()
                    })
                    state.isError.doIfChanged((appliedState as? PasswordState.Ready)?.isError, {
                        with (passwordContainerView) {
                            isErrorEnabled = it
                            if (it) {
                                error = getString(R.string.password_doesnt_match)
                            }
                        }
                    })
                    state.isButtonEnabled.doIfChanged((appliedState as? PasswordState.Ready)?.isButtonEnabled) {
                        nextButton.isEnabled = it
                    }
                }
            }
        })
        viewModel.actionData.observe(this, Observer { action ->
            when (action) {
                is PasswordAction.NavigateAction -> {
                    showContent()
                    navigate(action.navDirections)
                }
            }
        })
    }
}