package com.app.belcobtm.presentation.features.authorization.recover.wallet

import android.telephony.PhoneNumberFormattingTextWatcher
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.*
import com.app.belcobtm.presentation.core.mvvm.LoadingData
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
        viewModel.checkCredentialsLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    when {
                        it.data.first -> {
                            navigate(
                                R.id.to_sms_code_fragment,
                                bundleOf(
                                    SmsCodeFragment.TAG_PHONE to getPhone(),
                                    RecoverSeedFragment.TAG_PASSWORD to passwordView.getString(),
                                    SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_recover_seed_fragment
                                )
                            )
                            viewModel.checkCredentialsLiveData.value = null
                        }
                        it.data.second -> passwordView.showError(R.string.recover_wallet_incorrect_password)
                        else -> phoneView.showError(R.string.recover_wallet_incorrect_login)
                    }
                    showContent()
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
        viewModel.smsCodeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    viewModel.checkCredentialsLiveData.value = null
                    viewModel.smsCodeLiveData.value = null
                    navigate(R.id.to_recover_seed_fragment)
                    showContent()
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

    private fun isValidFields(phone: String, password: String): Boolean {
        if (phone.isEmpty() || password.isEmpty()) {
            showSnackBar(R.string.create_wallet_error_all_fields_required)
        } else if (password.length < 4) {
            showSnackBar(R.string.create_wallet_error_short_pass)
        }

        return phone.isNotBlank() && password.isNotBlank() && password.length >= 4
    }

    private fun checkCredentials() {
        val phone = getPhone()
        val password = passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.checkCredentials(phone, password)
        }
    }

    private fun updateNextButton() {
        nextButtonView.isEnabled = //getPhone().length == PHONE_LENGTH &&
            phoneView.getString().isNotEmpty()
                    && passwordView.getString().isNotEmpty()
    }

    private fun getPhone(): String = phoneView.getString().replace("[-() ]".toRegex(), "")

    private companion object {
        const val PHONE_MASK: String = "+[0] ([000]) [000]-[00]-[00]"
        private const val PHONE_LENGTH: Int = 12
    }
}