package com.app.belcobtm.ui.auth.login

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object LoginContract {

    interface Presenter : BaseMvpPresenter<View> {
        fun login(email: String?, password: String?)
        fun createWallet()
    }

    interface View : BaseMvpView {
        //        fun onSignInSuccess(token: String?)
        fun showProgress(show: Boolean)

        fun onLoginSuccess()
        fun showNoInternetError()
    }
}