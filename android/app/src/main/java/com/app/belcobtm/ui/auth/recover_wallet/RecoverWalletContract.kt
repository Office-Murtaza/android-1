package com.app.belcobtm.ui.auth.recover_wallet

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object RecoverWalletContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun attemptRecover(phone: String, pass: String)
        fun verifyCode(code: String)
    }

    interface View : BaseMvpView {
        fun onRecoverSuccess(seed: String)
        fun onSmsSuccess()
        fun openSmsCodeDialog(error: String? = null)
    }
}