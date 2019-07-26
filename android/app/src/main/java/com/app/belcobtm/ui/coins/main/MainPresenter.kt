package com.app.belcobtm.ui.coins.main

import com.app.belcobtm.App
import com.app.belcobtm.mvp.BaseMvpPresenterImpl
import com.app.belcobtm.util.pref


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