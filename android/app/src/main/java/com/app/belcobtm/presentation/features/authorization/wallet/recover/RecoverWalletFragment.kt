package com.app.belcobtm.presentation.features.authorization.wallet.recover

import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.afterTextChanged
import com.app.belcobtm.presentation.core.extensions.clearError
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.extensions.showError
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.presentation.features.sms.code.SmsCodeFragment
import com.redmadrobot.inputmask.MaskedTextChangedListener
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
            phoneContainerView.clearError()
            passwordView.clearError()
            recoverWallet()
        }
        phoneContainerView.editText?.afterTextChanged {
            nextButtonView.isEnabled =
                phoneContainerView.getString().isNotEmpty() && passwordView.getString().isNotEmpty()
        }
        passwordView.editText?.afterTextChanged {
            nextButtonView.isEnabled =
                phoneContainerView.getString().isNotEmpty() && passwordView.getString().isNotEmpty()
        }
        passwordView.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideKeyboard()
                recoverWallet()
                return@OnEditorActionListener true
            }
            false
        })
        MaskedTextChangedListener.installOn(phoneView, PHONE_MASK)
    }

    override fun initObservers() {
        viewModel.recoverWalletLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    navigate(
                        R.id.to_sms_code_fragment,
                        bundleOf(
                            SmsCodeFragment.TAG_PHONE to getPhone(),
                            SmsCodeFragment.TAG_NEXT_FRAGMENT_ID to R.id.to_recover_seed_fragment
                        )
                    )
                    showContent()
                    viewModel.recoverWalletLiveData.value = null
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.IncorrectLogin -> phoneContainerView.showError(R.string.recover_wallet_incorrect_login)
                        is Failure.IncorrectPassword -> passwordView.showError(R.string.recover_wallet_incorrect_password)
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
                    viewModel.recoverWalletLiveData.value = null
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
            showSnackBar(R.string.error_all_fields_required)
        } else if (password.length < 4) {
            showSnackBar(R.string.error_short_pass)
        }

        return phone.isNotBlank() && password.isNotBlank() && password.length >= 4
    }

    private fun recoverWallet() {
        val phone = getPhone()
        val password = passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.recoverWallet(phone, password)
        }
    }

    private fun getPhone(): String = phoneContainerView.getString().replace("[-() ]".toRegex(), "")

    private companion object {
        const val PHONE_MASK: String = "+[0] ([000]) [000]-[00]-[00]"
    }
}