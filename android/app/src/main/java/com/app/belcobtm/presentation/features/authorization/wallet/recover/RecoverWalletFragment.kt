package com.app.belcobtm.presentation.features.authorization.wallet.recover

import android.content.Intent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.ui.fragment.BaseFragment
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel

class RecoverWalletFragment : BaseFragment() {
    private val viewModel: RecoverWalletViewModel by viewModel()
    override val isToolbarEnabled: Boolean = false

    override val resourceLayout: Int = R.layout.activity_recover_wallet

    override fun initViews() {
        phonePickerView.registerCarrierNumberEditText(phoneView.editText)
    }

    override fun initListeners() {
        cancelButtonView.setOnClickListener { popBackStack() }
        nextButtonView.setOnClickListener { recoverWallet() }
        passwordView.editText?.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                hideSoftKeyboard()
                recoverWallet()
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun initObservers() {
        viewModel.recoverWalletLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    showSmsCodeDialog()
                    showContent()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSnackBar(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> showSnackBar(R.string.error_something_went_wrong)
                    }
                    showProgress()
                }
            }
        })
        viewModel.smsCodeLiveData.observe(this, Observer {
            when (it) {
                is LoadingData.Loading -> showProgress()
                is LoadingData.Success -> {
                    startActivity(Intent(activity, RecoverSeedActivity::class.java))
//                    finish()
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showSmsCodeDialog(it.errorType.message)
                        is Failure.NetworkConnection -> showSnackBar(R.string.error_internet_unavailable)
                        else -> showSnackBar(R.string.error_something_went_wrong)
                    }
                    showContent()
                }
            }
        })
    }

    private fun showSmsCodeDialog(error: String? = null) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        AlertDialog
            .Builder(requireContext())
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
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setView(view)
            .create()
            .show()
        view.findViewById<TextInputLayout>(R.id.til_sms_code).error = error
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
        val phone = phonePickerView.formattedFullNumber.replace("[- ]".toRegex(), "")
        val password = passwordView.getString()

        if (isValidFields(phone, password)) {
            viewModel.recoverWallet(phone, password)
        }
    }
}