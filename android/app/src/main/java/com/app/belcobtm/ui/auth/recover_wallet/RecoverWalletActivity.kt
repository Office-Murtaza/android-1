package com.app.belcobtm.ui.auth.recover_wallet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import com.app.belcobtm.ui.auth.recover_seed.RecoverSeedPhraseActivity
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.activity_create_wallet.*


class RecoverWalletActivity : BaseMvpActivity<RecoverWalletContract.View, RecoverWalletContract.Presenter>(),
    RecoverWalletContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)
        phone_ccp.registerCarrierNumberEditText(phone)
        bt_cancel.setOnClickListener { onBackPressed() }
        bt_next.setOnClickListener {
            attemptRecover()
        }

        pass.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                hideSoftKeyboard()
                attemptRecover()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun attemptRecover(){
        mPresenter.attemptRecover(
            phone_ccp.fullNumberWithPlus.toString(),
            pass.text.toString()
        )
    }

    override fun onRecoverSuccess(seed: String) {
        startActivity(Intent(this, RecoverWalletActivity::class.java))
    }

    override fun showProgress(show: Boolean) {
        runOnUiThread {
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

    override fun openSmsCodeDialog(error: String?) {
        val view = layoutInflater.inflate(R.layout.view_sms_code_dialog, null)
        val smsCode = view.findViewById<AppCompatEditText>(R.id.sms_code)
        AlertDialog
            .Builder(this)
            .setTitle(getString(R.string.verify_sms_code))
            .setPositiveButton(R.string.next)
            { _, _ ->
                val code = smsCode.text.toString()
                if (code.length != 4) {
                    openSmsCodeDialog(getString(R.string.error_sms_code_4_digits))
                } else {
                    mPresenter.verifyCode(code)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> onBackPressed() }
            .setView(view)
            .create()
            .show()
        val tilSmsCode = view.findViewById<TextInputLayout>(R.id.til_sms_code)
        tilSmsCode.error = error
    }

    override fun onSmsSuccess() {
        startActivity(Intent(this, RecoverSeedPhraseActivity::class.java))
        finish()
    }
}
