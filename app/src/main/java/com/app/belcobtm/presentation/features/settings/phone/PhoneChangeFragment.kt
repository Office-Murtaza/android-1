package com.app.belcobtm.presentation.features.settings.phone

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.observe
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentChangePhoneBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.settings.interactor.ERROR_UPDATE_PHONE_IS_SAME
import com.app.belcobtm.domain.settings.interactor.ERROR_UPDATE_PHONE_IS_USED
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel

class PhoneChangeFragment : BaseFragment<FragmentChangePhoneBinding>() {
    val viewModel by viewModel<PhoneChangeViewModel>()
    private var appliedState: LoadingData<PhoneChangeState>? = null
    override val isHomeButtonEnabled = true
    override var isMenuEnabled = true
    override val retryListener = View.OnClickListener {
        viewModel.onNextClick()
    }

    override fun FragmentChangePhoneBinding.initViews() {
        appliedState = null
        setToolbarTitle(R.string.update_phone_label)
    }

    override fun FragmentChangePhoneBinding.initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
        phoneView.addTextChangedListener {
            viewModel.onPhoneInput(it?.toString().orEmpty())
        }
    }

    override fun FragmentChangePhoneBinding.initObservers() {
        viewModel.stateData.listen(
            success = { state ->
                state.doIfChanged(appliedState) {
                    showContent()
                    phoneContainerView.isErrorEnabled = false
                }
                state.isNextButtonEnabled.doIfChanged(appliedState?.commonData?.isNextButtonEnabled) {
                    nextButton.isEnabled = it
                }
                state.isPhoneError.doIfChanged(appliedState?.commonData?.isPhoneError) {
                    phoneContainerView.isErrorEnabled = it
                }
            },
            error = {
                when ((it as? Failure.MessageError)?.code) {
                    ERROR_UPDATE_PHONE_IS_USED -> {
                        phoneContainerView.isErrorEnabled = true
                        phoneContainerView.error = getString(R.string.phone_is_already_used)
                    }
                    ERROR_UPDATE_PHONE_IS_SAME -> {
                        phoneContainerView.isErrorEnabled = true
                        phoneContainerView.error = getString(R.string.phone_is_the_same)
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
                is PhoneChangeAction.NavigateAction -> {
                    navigate(action.navDirections)
                }
                PhoneChangeAction.PopBackStackToSecurity -> {
                    popBackStack(R.id.security_fragment, false)
                }
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChangePhoneBinding =
        FragmentChangePhoneBinding.inflate(inflater, container, false)
}