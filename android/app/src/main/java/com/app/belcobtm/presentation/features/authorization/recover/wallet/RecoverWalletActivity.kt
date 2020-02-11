package com.app.belcobtm.presentation.features.authorization.recover.wallet

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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.Observer
import com.app.belcobtm.R
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.presentation.core.extensions.getString
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_wallet.*
import org.koin.android.viewmodel.ext.android.viewModel


class RecoverWalletActivity : AppCompatActivity() {
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
                    showProgress(false)
                }
                is LoadingData.Error -> {
                    when (it.errorType) {
                        is Failure.MessageError -> showError(it.errorType.message)
                        else -> Unit
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
                val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    showSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    verifyCode(code)
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


    private var userId: String = ""
    private val mDataManager = AuthDataManager()

    fun onSmsSuccess() {
        startActivity(Intent(this, RecoverSeedActivity::class.java))
        finish()
    }


    private fun verifyCode(code: String) {
        showProgress(true)
        mDataManager.verifySmsCode(userId, code).subscribe(
            {
                showProgress(false)
                onSmsSuccess()
            },
            { error: Throwable ->
                showProgress(false)
                if (error is ServerException) {
                    showSmsCodeDialog(error.errorMessage)
                } else {
                    showError(error.message)
                }

            }
        )
    }

    private fun showProgress(show: Boolean) {
        runOnUiThread {
            val progress = findViewById<FrameLayout?>(R.id.progress)
            if (progress != null) {
                val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
                progress.animate()
                    .setDuration(shortAnimTime)
                    .alpha((if (show) 1 else 0).toFloat())
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            progress.visibility = if (show) View.VISIBLE else View.GONE
                        }
                    })
            }
        }
    }

    private fun hideSoftKeyboard(): Boolean {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        return true
    }

    private fun showError(error: String?) {
        showError(error, Snackbar.LENGTH_SHORT)
    }

    private fun showError(stringResId: Int) {
        showError(getString(stringResId), Snackbar.LENGTH_SHORT)
    }

    private fun showError(error: String?, @Snackbar.Duration duration: Int) {
        runOnUiThread {
            val toastLength = if (duration == Snackbar.LENGTH_SHORT) Toast.LENGTH_SHORT else Toast.LENGTH_LONG

            var _error = error
            if (_error.isNullOrEmpty()) _error = "Unknown error appeared"

            val containerView = findViewById<View>(R.id.container)
            if (containerView != null) {
                val snackbar = Snackbar.make(containerView, _error, Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.error_color_material_light))
                snackbar.show()
            } else {
                Toast.makeText(this, _error, toastLength).show()
            }
        }
    }
}


//    fun onRecoverSuccess(seed: String) {
//        startActivity(Intent(this, RecoverWalletActivity::class.java))
//    }