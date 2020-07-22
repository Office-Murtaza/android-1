package com.app.belcobtm.presentation.features.authorization.recover.wallet

import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import kotlinx.android.synthetic.main.fragment_recover_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel


class RecoverWalletFragment : BaseFragment() {
    private val viewModel: RecoverWalletViewModel by viewModel()
    private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(requireContext()) }

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val resourceLayout: Int = R.layout.fragment_recover_wallet

    override fun initViews() {
        super.initViews()
        setToolbarTitle(R.string.recover_wallet_screen_title)
    }

    override fun initListeners() {
        nextButtonView.setOnClickListener {
            phoneView.clearError()
            passwordView.clearError()
            checkCredentials()
        }
        phoneView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.afterTextChanged { updateNextButton() }
        passwordView.editText?.actionDoneListener {
            hideKeyboard()
            checkCredentials()
        }
        phoneEditView.addTextChangedListener(PhoneNumberFormattingTextWatcher())
    }

    override fun initObservers() {
        viewModel.checkCredentialsLiveData.listen({
            var isValid = true

            if (!it.first) {
                isValid = false
                phoneView.showError(R.string.recover_wallet_incorrect_login)
            } else {
                phoneView.clearError()
            }

            if (!it.second) {
                isValid = false
                passwordView.showError(R.string.recover_wallet_incorrect_password)
            } else {
                passwordView.clearError()
            }

            if (isValid) {
                viewModel.checkCredentialsLiveData.value = null
                navigate(
                    R.id.to_sms_code_fragment,
                    bundleOf(
                        SmsCodeFragment.TAG_PHONE to getPhone(),
                        RecoverSeedFragment.TAG_PASSWORD to passwordView.getString(),
                        SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_recover_seed_fragment
                    )
                )
            }
        })
        viewModel.smsCodeLiveData.listen({
            viewModel.checkCredentialsLiveData.value = null
            viewModel.smsCodeLiveData.value = null
            navigate(R.id.to_recover_seed_fragment)
            showContent()
        })
    }

    private fun isValidFields(phone: String, password: String): Boolean {
        val isFieldsNotEmpty = phone.isNotEmpty() && password.isNotEmpty()
        if (isFieldsNotEmpty) {
            showSnackBar(R.string.recover_wallet_error_all_fields_required)
        }

        return isFieldsNotEmpty
    }

    private fun checkCredentials() {
        val phone = getPhone()
        val password = passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.checkCredentials(phone, password)
        }
    }

    private fun updateNextButton() {
        nextButtonView.isEnabled = phoneView.getString().isNotEmpty()
                && isValidMobileNumber(phoneView.getString())
                && passwordView.getString().isNotEmpty()
    }

    private fun isValidMobileNumber(phone: String): Boolean = if (phone.isNotBlank()) {
        try {
            val number = PhoneNumberUtil.createInstance(requireContext()).parse(phone, "")
            phoneUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    } else {
        false
    }

    private fun getPhone(): String = phoneView.getString().replace("[-() ]".toRegex(), "")
}