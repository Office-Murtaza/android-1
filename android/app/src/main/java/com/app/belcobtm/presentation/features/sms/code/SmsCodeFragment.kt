package com.app.belcobtm.presentation.features.sms.code

import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.actionDoneListener
import com.app.belcobtm.presentation.core.extensions.afterTextChanged
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_sms_code.*
import org.koin.android.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SmsCodeFragment : BaseFragment() {
    private val viewModel: SmsCodeViewModel by viewModel { parametersOf(arguments?.getString(TAG_PHONE)) }
    private var isResendClicked: Boolean = false
    override val resourceLayout: Int = R.layout.fragment_sms_code
    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true

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
        pinEntryView.afterTextChanged { nextButtonView.isEnabled = pinEntryView.getString().length >= SMS_CODE_LENGTH }
    }

    override fun initObservers() {
        viewModel.smsLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is LoadingData.Loading -> showLoading()
                is LoadingData.Success -> {
                    if (isResendClicked) {
                        showResendDialog()
                    }
                    showContent()

                    //TODO for remove
                    println("SMS code " + it.data)
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSnackBar(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> showSnackBar(R.string.error_something_went_wrong)
                    }
                    showContent()
                }
            }
        })
    }

    private fun openNextScreen() {
        val isSuccessLoadingData =
            (viewModel.smsLiveData.value as? LoadingData.Success)?.data == pinEntryView.getString()
        when {
            isSuccessLoadingData -> navigate(requireArguments().getInt(TAG_NEXT_FRAGMENT_ID), requireArguments())
            !isSuccessLoadingData && pinEntryView.getString().length == SMS_CODE_LENGTH -> showSnackBar(R.string.sms_code_screen_invalid_code)
        }
    }

    private fun showResendDialog() {
        isResendClicked = false
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sms_code_screen_resend_title)
            .setMessage(R.string.sms_code_screen_resend_description)
            .setNegativeButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        private const val SMS_CODE_LENGTH: Int = 4
        const val TAG_PHONE: String = "sms_code_screen_phone"
        const val TAG_NEXT_FRAGMENT_ID: String = "sms_code_screen_next_fragment_id"
    }
}