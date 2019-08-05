package com.app.belcobtm.ui.main.coins.settings.check_pass

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object CheckPassContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun checkPass(pass: String)
        fun requestSeed()
    }

    interface View : BaseMvpView {
        fun onPassConfirmed()
        fun onSeedGot(seed: String?)
    }
}