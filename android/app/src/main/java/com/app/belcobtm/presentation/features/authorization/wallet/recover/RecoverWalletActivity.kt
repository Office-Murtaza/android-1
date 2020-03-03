package com.app.belcobtm.presentation.features.authorization.wallet.recover

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.helper.AlertHelper
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.BaseActivity
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel


class RecoverWalletActivity : BaseActivity() {
    private val viewModel: RecoverWalletViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)
        initViews()
        initListeners()
        initObservers()
    }

    private fun initViews() {
        phonePickerView.registerCarrierNumberEditText(phoneView.editText)
    }

    private fun initListeners() {
        cancelButtonView.setOnClickListener { onBackPressed() }
        nextButtonView.setOnClickListener { recoverWallet() }
        passwordView.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideSoftKeyboard()
                recoverWallet()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun initObservers() {
        viewModel.recoverWalletLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress(true)
                is LoadingData.Success -> {
                    showSmsCodeDialog()
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
                    showProgress(false)
                    startActivity(Intent(this, RecoverSeedActivity::class.java))
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

    private fun showSmsCodeDialog(error: String? = null) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code).text.toString()
                if (smsCode.length != 4) {
                    showSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    viewModel.verifySmsCode(smsCode)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> onBackPressed() }
            .setView(view)
            .create()
            .show()
        view.findViewById<TextInputLayout>(R.id.til_sms_code).error = error
    }

    private fun isValidFields(phone: String, password: String): Boolean {
        if (phone.isEmpty() || password.isEmpty()) {
            showError(R.string.error_all_fields_required)
        } else if (password.length < 4) {
            showError(R.string.error_short_pass)
        }

        return phone.isNotBlank() && password.isNotBlank() && password.length >= 4
    }

    private fun recoverWallet() {
        val phone = phonePickerView.formattedFullNumber.replace("[- ]".toRegex(), "")
        val password = passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.recoverWallet(phone, password)
        }
    }
}