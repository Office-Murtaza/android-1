package com.app.belcobtm.ui.auth.recover_seed

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object RecoverSeedContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun verifySeed(seed: String)

    }

    interface View : BaseMvpView {
        fun onSeedVerifyed()

    }
}