package com.app.belcobtm.ui.coins.main

import com.app.belcobtm.api.model.response.GetCoinsResponse
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object MainContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun checkPinEntered()
    }

    interface View : BaseMvpView {
        fun onPinSaved()
        fun onPinNotSaved()
        fun onTokenNotSaved()

    }
}