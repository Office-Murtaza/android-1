package com.app.belcobtm.presentation.features.sms.code

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.app.belcobtm.R
import com.app.belcobtm.databinding.FragmentSmsCodeBinding
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.authorization.interactor.AUTH_ERROR_PHONE_NOT_SUPPORTED
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SmsCodeFragment : BaseFragment<FragmentSmsCodeBinding>() {
    private val viewModel: SmsCodeViewModel by viewModel {
        parametersOf(
            requireArguments().getString(
                TAG_PHONE
            )
        )
    }
    private var isResendClicked: Boolean = false
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener =
        View.OnClickListener { viewModel.sendSmsToDevice() }

    override fun FragmentSmsCodeBinding.initViews() {
        setToolbarTitle(R.string.sms_code_screen_title)
        setToolbarTitle()
        setNextButtonTitle()
        isMenuEnabled = false
    }

    override fun FragmentSmsCodeBinding.initListeners() {
        resendCodeButtonView.setOnClickListener {
            isResendClicked = true
            pinEntryView.setText("")
            viewModel.sendSmsToDevice()
        }
        nextButtonView.setOnClickListener { onNextClick() }
        pinEntryView.actionDoneListener { onNextClick() }
        pinEntryView.afterTextChanged {
            nextButtonView.isEnabled = pinEntryView.getString().length >= SMS_CODE_LENGTH
            errorMessageView.invisible()
            pinEntryView.isError = false
        }
    }

    override fun FragmentSmsCodeBinding.initObservers() {
        viewModel.smsLiveData.listen(
            success = {
                errorTextView.hide()
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
                        if (it.code == AUTH_ERROR_PHONE_NOT_SUPPORTED) {
                            showContent()
                            errorTextView.show()
                        } else {
                            showSnackBar(it.message ?: "")
                            showContent()
                        }
                    }
                    is Failure.ServerError -> if (it.message.equals(
                            "No value for errorMsg",
                            true
                        )
                    ) {
                        showSnackBar("Incorrect phone number")
                        showContent()
                        popBackStack()
                    } else {
                        showErrorServerError()
                    }
                    else -> showErrorSomethingWrong()
                }
            })
        viewModel.phoneUpdateData.listen(
            success = {
                if (it) {
                    showSnackBar(R.string.phone_updated)
                    openNextScreen()
                } else {
                    baseErrorHandler(Failure.ServerError())
                }
            }
        )
    }

    private fun onNextClick() {
        openNextScreen()
    }

    private fun openNextScreen() {
        val isSuccessLoadingData =
            (viewModel.smsLiveData.value as? LoadingData.Success)?.data == binding.pinEntryView.getString()
        when {
            isSuccessLoadingData -> {
                val nextScreenId = requireArguments().getInt(TAG_NEXT_FRAGMENT_ID, -1)
                if (nextScreenId >= 0) {
                    navigate(requireArguments().getInt(TAG_NEXT_FRAGMENT_ID), requireArguments())
                } else {
                    setFragmentResult(REQUEST_KEY, bundleOf(REQUEST_TAG_IS_SUCCESS to true))
                    popBackStack()
                }
            }
            !isSuccessLoadingData && binding.pinEntryView.getString().length == SMS_CODE_LENGTH -> {
                binding.errorMessageView.show()
                binding.pinEntryView.isError = true
            }
        }
    }

    private fun showResendDialog() {
        isResendClicked = false
        AlertHelper.showToastShort(requireContext(), R.string.sms_code_screen_resend)
    }

    private fun setToolbarTitle() {
        setToolbarTitle(R.string.sms_code_screen_title)
    }

    private fun setNextButtonTitle() {
        binding.nextButtonView.text = getString(R.string.next)
    }

    companion object {
        private const val SMS_CODE_LENGTH: Int = 4
        const val TAG_PHONE: String = "sms_code_screen_phone"
        const val TAG_NEXT_FRAGMENT_ID: String = "sms_code_screen_next_fragment_id"

        const val REQUEST_KEY = "sms_code_request_key"
        const val REQUEST_TAG_IS_SUCCESS = "sms_code_is_success"
    }

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentSmsCodeBinding =
        FragmentSmsCodeBinding.inflate(inflater, container, false)
}