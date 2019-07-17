package com.app.belcobtm.ui.auth.create_wallet

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object CreateWalletContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun attemptCreateWallet(phone: String, pass: String, confirmPass: String)
        fun verifyCode(code: String)
    }

    interface View : BaseMvpView {
        fun openSmsCodeDialog(error: String? = null)
        fun onWalletCreated(seed: String)
    }
}