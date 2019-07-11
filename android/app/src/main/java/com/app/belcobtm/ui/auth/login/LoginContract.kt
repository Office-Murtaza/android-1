package com.app.belcobtm.ui.auth.login

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object LoginContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun attemptLogin(phone: String, pass: String)
    }

    interface View : BaseMvpView {
        fun showProgress(show: Boolean)
        fun onLoginSuccess(seed: String)
    }
}