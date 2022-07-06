package com.belcobtm.presentation.screens.sms.code

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import com.belcobtm.R
import com.belcobtm.databinding.FragmentSmsCodeBinding
import com.belcobtm.domain.Failure
import com.belcobtm.domain.authorization.interactor.AUTH_ERROR_PHONE_NOT_SUPPORTED
import com.belcobtm.presentation.tools.extensions.getString
import com.belcobtm.presentation.tools.extensions.hide
import com.belcobtm.presentation.tools.extensions.setText
import com.belcobtm.presentation.tools.extensions.show
import com.belcobtm.presentation.core.helper.AlertHelper
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SmsCodeFragment : BaseFragment<FragmentSmsCodeBinding>() {

    private val viewModel: SmsCodeViewModel by viewModel {
        parametersOf(requireArguments().getString(TAG_PHONE))
    }
    private var isResendClicked: Boolean = false
    override val isBackButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.sendSmsToDevice() }

    override fun FragmentSmsCodeBinding.initViews() {
        setToolbarTitle(R.string.sms_code_screen_title)
        setToolbarTitle()
        isMenuEnabled = false
    }

    override fun FragmentSmsCodeBinding.initListeners() {
        resendCodeButtonView.setOnClickListener {
            isResendClicked = true
            codeEntryView.setText("")
            viewModel.sendSmsToDevice()
        }

        codeEntryView.editText?.addTextChangedListener {
            codeEntryView.isErrorEnabled = false
        }
        verifyButton.setOnClickListener {
            viewModel.verifyCode(codeEntryView.getString())
        }
    }

    override fun FragmentSmsCodeBinding.initObservers() {
        viewModel.smsLiveData.listen(
            success = {
                errorTextView.hide()
                if (isResendClicked) {
                    showResendDialog()
                }
            },
            error = ::handleError
        )
        viewModel.smsVerifyLiveData.listen(
            success = ::openNextScreen,
            error = ::handleError
        )
    }

    private fun handleError(it: Failure?) {
        when (it) {
            is Failure.NetworkConnection -> showErrorNoInternetConnection()
            is Failure.MessageError -> {
                if (it.code == AUTH_ERROR_PHONE_NOT_SUPPORTED) {
                    showContent()
                    binding.errorTextView.text = it.message ?: ""
                    binding.errorTextView.show()
                } else {
                    showToast(it.message ?: "")
                    showContent()
                }
            }
            is Failure.ServerError -> if (it.message.equals(
                    "No value for errorMsg",
                    true
                )
            ) {
                showToast("Incorrect phone number")
                showContent()
                popBackStack()
            } else {
                showErrorServerError()
            }
            else -> showErrorSomethingWrong()
        }
    }

    private fun openNextScreen(correctCode: Boolean) {
        if (correctCode) {
            if (checkVerificationTarget()) return
            val nextScreenId = requireArguments().getInt(TAG_NEXT_FRAGMENT_ID, -1)
            if (nextScreenId >= 0) {
                navigate(requireArguments().getInt(TAG_NEXT_FRAGMENT_ID), requireArguments())
            } else {
                setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_TAG_IS_SUCCESS to true))
                popBackStack()
            }
        } else {
            binding.codeEntryView.isErrorEnabled = true
            binding.codeEntryView.error = getString(R.string.sms_code_screen_invalid_code)
        }
    }

    private fun checkVerificationTarget(): Boolean =
        when (requireArguments().getString(TAG_VERIFICATION_TARGET)) {
            PHONE_UPDATE_VERIFICATION -> {
                setFragmentResult(REQUEST_KEY, bundleOf(BUNDLE_KEY_PHONE_UPDATE_VERIFICATION to true))
                popBackStack(R.id.security_fragment, false)
                true
            }
            else -> false
        }

    private fun showResendDialog() {
        isResendClicked = false
        AlertHelper.showToastShort(requireContext(), R.string.sms_code_screen_resend)
    }

    private fun setToolbarTitle() {
        setToolbarTitle(R.string.sms_code_screen_title)
    }

    companion object {

        const val TAG_PHONE: String = "sms_code_screen_phone"
        const val TAG_NEXT_FRAGMENT_ID: String = "sms_code_screen_next_fragment_id"
        const val TAG_VERIFICATION_TARGET = "sms_code_screen_phone_update_verification"

        const val REQUEST_KEY = "sms_code_request_key"
        const val REQUEST_TAG_IS_SUCCESS = "sms_code_is_success"
        const val BUNDLE_KEY_PHONE_UPDATE_VERIFICATION = "phone_update_verification_success"

        const val PHONE_UPDATE_VERIFICATION = "phone_update_verification"
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentSmsCodeBinding =
        FragmentSmsCodeBinding.inflate(inflater, container, false)

}
