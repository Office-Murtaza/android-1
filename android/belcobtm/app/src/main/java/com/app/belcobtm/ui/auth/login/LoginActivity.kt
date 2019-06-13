package com.app.belcobtm.ui.auth.login

import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.app.belcobtm.R
import com.app.belcobtm.mvp.BaseMvpActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseMvpActivity<LoginContract.View, LoginContract.Presenter>(), LoginContract.View {

    override var mPresenter: LoginContract.Presenter = LoginPresenter()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        password.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun attemptLogin() {
        hideSoftKeyboard()
        //todo
    }

    private fun isEmailValid(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with logic
        return password.length > 4
    }


    override fun showProgress(show: Boolean) {
        runOnUiThread {
            // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
            // for very easy animations. If available, use these APIs to fade-in
            // the progress spinner.
            //val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()

        }
    }

    override fun onLoginSuccess() {
        runOnUiThread {
            //todo
        }
    }

    override fun showNoInternetError() {
        showProgress(false)
        val snackbar = Snackbar.make(container, R.string.error_no_internet_no_socket, Snackbar.LENGTH_LONG)
        val view = snackbar.view
        val tv = view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        tv.setTextColor(resources.getColor(R.color.colorErrorOrange))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv.textAlignment = View.TEXT_ALIGNMENT_VIEW_END
        } else {
            tv.gravity = Gravity.END.and(Gravity.RIGHT)
        }

        snackbar.show()
    }
}
