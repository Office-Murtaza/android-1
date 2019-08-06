package com.app.belcobtm.ui.main.coins.settings.phone

import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object ShowPhoneContract {
    interface Presenter : BaseMvpPresenter<View> {
    }

    interface View : BaseMvpView {
        fun onPhoneReceived(phone: String?)
    }
}