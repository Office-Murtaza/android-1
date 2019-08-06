package com.app.belcobtm.ui.auth.login

import android.content.Intent
import android.os.Bundle
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_create_wallet.*


class LoginActivity : BaseMvpActivity<LoginContract.View, LoginContract.Presenter>(),
    LoginContract.View {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_wallet)
        phone_ccp.registerCarrierNumberEditText(phone)
        bt_cancel.setOnClickListener { onBackPressed() }
        bt_next.setOnClickListener {
            mPresenter.attemptLogin(
                phone_ccp.fullNumberWithPlus.toString(),
                edit_text.text.toString()
            )
        }
    }

    override fun onLoginSuccess(seed: String) {
        startActivity(Intent(this, LoginActivity::class.java))
    }

}
