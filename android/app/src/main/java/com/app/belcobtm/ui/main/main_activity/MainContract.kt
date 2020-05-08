package com.app.belcobtm.ui.main.main_activity

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object MainContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun checkPinEntered()
        fun isApiSeedEmpty():Boolean
    }

    interface View : BaseMvpView {
        fun onPinSaved()
        fun onPinNotSaved()
        fun onTokenNotSaved()
        fun onSeedNotSaved()
    }
}