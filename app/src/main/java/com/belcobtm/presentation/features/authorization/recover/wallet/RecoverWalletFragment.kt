package com.belcobtm.presentation.features.authorization.recover.wallet

import android.telephony.PhoneNumberFormattingTextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.belcobtm.R
import com.belcobtm.databinding.FragmentRecoverWalletBinding
import com.belcobtm.presentation.core.extensions.*
import com.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.belcobtm.presentation.features.authorization.recover.seed.RecoverSeedFragment
import com.belcobtm.presentation.features.sms.code.SmsCodeFragment
import org.koin.android.viewmodel.ext.android.viewModel

class RecoverWalletFragment : BaseFragment<FragmentRecoverWalletBinding>() {

    private val viewModel: RecoverWalletViewModel by viewModel()

    override val isToolbarEnabled: Boolean = true
    override val isHomeButtonEnabled: Boolean = true
    override val retryListener: View.OnClickListener = View.OnClickListener { checkCredentials() }

    override fun FragmentRecoverWalletBinding.initViews() {
        setToolbarTitle(R.string.recover_wallet_screen_title)
    }

    override fun FragmentRecoverWalletBinding.initListeners() {
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

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecoverWalletBinding =
        FragmentRecoverWalletBinding.inflate(inflater, container, false)

    override fun FragmentRecoverWalletBinding.initObservers() {
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
                binding.passwordView.clearError()
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
        val password = binding.passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.checkCredentials(phone, password)
        }
    }

    private fun updateNextButton() {
        binding.nextButtonView.isEnabled = binding.phoneView.getString().isNotEmpty()
                && viewModel.isValidMobileNumber(binding.phoneView.getString())
                && binding.passwordView.getString().isNotEmpty()
    }

    private fun getPhone(): String = binding.phoneView.getString().replace("[-() ]".toRegex(), "")
}