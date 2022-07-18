package com.belcobtm.presentation.screens.settings.security.phone

import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import com.belcobtm.R
import com.belcobtm.databinding.FragmentUpdatePhoneBinding
import com.belcobtm.domain.Failure
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.screens.settings.security.phone.UpdatePhoneViewModel.Companion.ERROR_UPDATE_PHONE_IS_SAME
import com.belcobtm.presentation.screens.settings.security.phone.UpdatePhoneViewModel.Companion.ERROR_UPDATE_PHONE_IS_USED
import com.belcobtm.presentation.screens.sms.code.SmsCodeFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdatePhoneFragment : BaseFragment<FragmentUpdatePhoneBinding>() {

    val viewModel by viewModel<UpdatePhoneViewModel>()

    private var appliedState: LoadingData<UpdatePhoneState>? = null
    override val isBackButtonEnabled = true
    override var isMenuEnabled = true
    override val retryListener = View.OnClickListener {
        viewModel.onNextClick()
    }

    override fun FragmentUpdatePhoneBinding.initViews() {
        appliedState = null
        setToolbarTitle(R.string.update_phone_label)
    }

    override fun FragmentUpdatePhoneBinding.initListeners() {
        nextButton.setOnClickListener {
            viewModel.onNextClick()
        }
        phoneContainerView.editText?.addTextChangedListener {
            viewModel.onPhoneInput(it?.toString().orEmpty())
        }
        phoneView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun FragmentUpdatePhoneBinding.initObservers() {
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
                is UpdatePhoneAction.GoToSmsVerification -> {
                    navigate(
                        R.id.phone_change_to_sms_fragment,
                        bundleOf(
                            SmsCodeFragment.TAG_PHONE to action.phone,
                            SmsCodeFragment.TAG_VERIFICATION_TARGET to SmsCodeFragment.PHONE_UPDATE_VERIFICATION
                        )
                    )
                }
            }
        }
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentUpdatePhoneBinding =
        FragmentUpdatePhoneBinding.inflate(inflater, container, false)

}
