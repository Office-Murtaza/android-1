package com.app.belcobtm.ui.auth.recover_wallet

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_create_wallet.*


class RecoverWalletActivity : BaseMvpActivity<RecoverWalletContract.View, RecoverWalletContract.Presenter>(),
    RecoverWalletContract.View {

    override var mPresenter: RecoverWalletContract.Presenter = RecoverWalletPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)
        phone_ccp.registerCarrierNumberEditText(phone)
        bt_cancel.setOnClickListener { onBackPressed() }
        bt_next.setOnClickListener {
            mPresenter.attemptLogin(
                phone_ccp.fullNumberWithPlus.toString(),
                pass.text.toString()
            )
        }
    }

    override fun onLoginSuccess(seed: String) {
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


}
