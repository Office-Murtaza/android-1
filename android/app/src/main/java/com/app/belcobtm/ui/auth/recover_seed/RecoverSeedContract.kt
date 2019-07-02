package com.app.belcobtm.ui.auth.recover_seed

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object RecoverSeedContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun attemptRecoverWallet(phone: String, pass: String)
        fun verifyCode(code: String)
    }

    interface View : BaseMvpView {
        fun openSmsCodeDialog(error: String? = null)
        fun showProgress(show: Boolean)
    }
}