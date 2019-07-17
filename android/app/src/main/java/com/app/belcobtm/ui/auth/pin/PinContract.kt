package com.app.belcobtm.ui.auth.pin

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object PinContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun checkCryptoPin(pin: String)
        fun savePin(pin: String)
        fun vibrate(milliseconds: Long)
        fun vibrateError()
    }

    interface View : BaseMvpView {
        fun closeScreenAndContinue()
        fun pinNotMatch()
    }
}