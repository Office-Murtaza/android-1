package com.app.belcobtm.presentation.features.authorization.recover.wallet

import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.View
import androidx.core.os.bundleOf
import com.app.belcobtm.R
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import kotlinx.android.synthetic.main.fragment_recover_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel


class RecoverWalletFragment : BaseFragment() {

    private val viewModel: RecoverWalletViewModel by viewModel()

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val resourceLayout: Int = R.layout.fragment_recover_wallet
    override val retryListener: View.OnClickListener = View.OnClickListener { checkCredentials() }

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
        viewModel.checkCredentialsLiveData.listen(success = {
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
                navigate(
                    R.id.to_sms_code_fragment,
                    bundleOf(
                        SmsCodeFragment.TAG_PHONE to getPhone(),
                        RecoverSeedFragment.TAG_PASSWORD to passwordView.getString(),
                        SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_recover_seed_fragment
                    )
                )
                passwordView.clearText()
                viewModel.checkCredentialsLiveData.value = null
            }
        })
    }

    private fun isValidFields(phone: String, password: String): Boolean {
        val isEmptyFields = phone.isEmpty() || password.isEmpty()
        if (isEmptyFields) {
            showSnackBar(R.string.recover_wallet_error_all_fields_required)
        }

        return !isEmptyFields
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
                && viewModel.isValidMobileNumber(phoneView.getString())
                && passwordView.getString().isNotEmpty()
    }

    private fun getPhone(): String = phoneView.getString().replace("[-() ]".toRegex(), "")
}