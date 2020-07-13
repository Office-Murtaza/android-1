package com.app.belcobtm.presentation.features.authorization.wallet.create

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.ui.auth.seed.SeedPhraseActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel

class CreateWalletActivity : BaseActivity() {
    private val viewModel: CreateWalletViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wallet)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        phonePickerView.registerCarrierNumberEditText(phoneContainerView.editText)
    }

    private fun initListeners() {
        cancelButtonView.setOnClickListener { onBackPressed() }
        nextButtonView.setOnClickListener { createWallet() }
        confirmPasswordView.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideSoftKeyboard()
                createWallet()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun initObservers() {
        viewModel.createWalletLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress(true)
                is LoadingData.Success -> {
                    viewModel.createWalletLiveData.value = null
                    showSmsCodeDialog(null)
                    showProgress(false)
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showError(it.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    showProgress(false)
                }
            }
        })

        viewModel.smsCodeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress(true)
                is LoadingData.Success -> {
                    viewModel.smsCodeLiveData.value = null
                    SeedPhraseActivity.startActivity(this, it.data)
                    showProgress(false)
                    finish()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSmsCodeDialog(it.errorType.message)
                        is Failure.NetworkConnection -> showError(R.string.error_internet_unavailable)
                        else -> showError(R.string.error_something_went_wrong)
                    }
                    showProgress(false)
                }
            }
        })
    }

    private fun isValidFields(phone: String, password: String, confirmPassword: String): Boolean = when {
        !phonePickerView.isValidFullNumber -> {
            showError(R.string.error_invalid_phone_number)
            false
        }
        phone.isEmpty() || phone.isEmpty() || confirmPassword.isEmpty() -> {
            showError(R.string.error_all_fields_required)
            false
        }
        password.length < PASSWORD_MIN_LENGTH -> {
            showError(R.string.error_short_pass)
            false
        }
        password != confirmPassword -> {
            showError(R.string.error_confirm_pass)
            false
        }
        else -> true
    }

    private fun createWallet() {
        val phone = phonePickerView.formattedFullNumber
        val password = passwordView.getString()
        val confirmPassword = confirmPasswordView.text.toString()
        if (isValidFields(phone, password, confirmPassword)) {
            viewModel.createWallet(phone.replace("[- )(]".toRegex(), ""), passwordView.getString())
        }
    }

    private fun showSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next) { _, _ ->
                val code = view.findViewById<AppCompatEditText>(R.id.sms_code).text.toString()
                if (code.length != SMS_CODE_LENGTH) {
                    showSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    viewModel.verifySmsCode(code)
                }
            }.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setView(view)
            .create()
            .show()
        view.findViewById<TextInputLayout>(R.id.til_sms_code).error = error
    }

    companion object {
        private const val PASSWORD_MIN_LENGTH: Int = 6
        private const val SMS_CODE_LENGTH = 4
    }
}
