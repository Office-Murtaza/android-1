package com.app.belcobtm.ui.auth.login

import com.app.belcobtm.mvp.BaseMvpPresenterImpl


class LoginPresenter : BaseMvpPresenterImpl<LoginContract.View>(), LoginContract.Presenter {

    //private val mDataManager = LoginDataManager()

    override fun attachView(view: LoginContract.View) {
        super.attachView(view)
    }

    override fun login(email: String?, password: String?) {
        mView?.showProgress(true)
        //todo
    }


}