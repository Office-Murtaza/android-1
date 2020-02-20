package com.app.belcobtm.ui.main.main_activity

import com.app.belcobtm.App
import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import com.app.belcobtm.presentation.core.pref


class MainPresenter : BaseMvpPresenterImpl<MainContract.View>(),
    MainContract.Presenter {

    override fun checkPinEntered() {
        val pin = App.appContext().pref.getPin()
        val token = App.appContext().pref.getSessionApiToken()
        when {
            token == null -> mView?.onTokenNotSaved()
            pin != null -> mView?.onPinSaved()
            else -> mView?.onPinNotSaved()
        }
    }
}