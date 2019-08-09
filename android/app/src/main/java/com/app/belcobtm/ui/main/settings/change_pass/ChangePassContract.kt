package com.app.belcobtm.ui.main.coins.settings.change_pass

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object ChangePassContract {
    interface Presenter : BaseMvpPresenter<View> {
        fun changePass(oldPass: String, newPass: String, confirmNewPass: String)
        fun changePin(oldPin: String, newPin: String, confirmNewPin: String)
    }

    interface View : BaseMvpView {
        fun onPassChanged()
        fun onPinChanged()
    }
}