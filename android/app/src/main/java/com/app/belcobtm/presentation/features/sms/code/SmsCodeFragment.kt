package com.app.belcobtm.presentation.features.sms.code

import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_sms_code.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SmsCodeFragment : BaseFragment() {
    private val viewModel: SmsCodeViewModel by viewModel { parametersOf(requireArguments().getString(TAG_PHONE)) }
    private var isResendClicked: Boolean = false
    override val resourceLayout: Int = R.layout.fragment_sms_code
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { viewModel.sendSmsToDevice() }

    override fun initViews() {
        setToolbarTitle(R.string.sms_code_screen_title)
    }

    override fun initListeners() {
        resendCodeButtonView.setOnClickListener {
            isResendClicked = true
            pinEntryView.setText("")
            viewModel.sendSmsToDevice()
        }
        nextButtonView.setOnClickListener { openNextScreen() }
        pinEntryView.actionDoneListener { openNextScreen() }
        pinEntryView.afterTextChanged {
            nextButtonView.isEnabled = pinEntryView.getString().length >= SMS_CODE_LENGTH
            errorMessageView.invisible()
            pinEntryView.isError = false
        }
    }

    override fun initObservers() {
        viewModel.smsLiveData.listen(
            success = {
                if (isResendClicked) {
                    showResendDialog()
                }

                //TODO for remove
                println("SMS code $it")
            },
            error = {
                when (it) {
                    is Failure.NetworkConnection -> showErrorNoInternetConnection()
                    is Failure.MessageError -> {
                        showSnackBar(it.message ?: "")
                        showContent()
                    }
                    is Failure.ServerError -> if (it.message.equals("No value for errorMsg", true)) {
                        showSnackBar("Incorrect phone number")
                        showContent()
                        popBackStack()
                    } else {
                        showErrorServerError()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
    }

    private fun openNextScreen() {
        val isSuccessLoadingData =
            (viewModel.smsLiveData.value as? LoadingData.Success)?.data == pinEntryView.getString()
        when {
            isSuccessLoadingData -> navigate(requireArguments().getInt(TAG_NEXT_FRAGMENT_ID), requireArguments())
            !isSuccessLoadingData && pinEntryView.getString().length == SMS_CODE_LENGTH -> {
                errorMessageView.show()
                pinEntryView.isError = true
            }
        }
    }

    private fun showResendDialog() {
        isResendClicked = false
        AlertHelper.showToastShort(requireContext(), R.string.sms_code_screen_resend)
    }

    companion object {
        private const val SMS_CODE_LENGTH: Int = 4
        const val TAG_PHONE: String = "sms_code_screen_phone"
        const val TAG_NEXT_FRAGMENT_ID: String = "sms_code_screen_next_fragment_id"
    }
}