package com.app.belcobtm.ui.auth.login

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref
import io.reactivex.Observable


class LoginPresenter : BaseMvpDIPresenterImpl<LoginContract.View, AuthDataManager>(),
    LoginContract.Presenter {

    private var userId: String = ""
    private var seed: String = ""

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun attemptLogin(phone: String, pass: String) {
        mView?.showProgress(true)
        mDataManager.login(phone, pass)
            .flatMap { response ->
                App.appContext().pref.setSessionApiToken(response.value?.accessToken)
                App.appContext().pref.setRefreshApiToken(response.value?.refreshToken)
                App.appContext().pref.setUserId(response.value?.userId)
                mDataManager.updateToken()

                return@flatMap Observable.just(response)
            }
            .subscribe({ response: Optional<AuthResponse> ->
                mView?.showProgress(false)
                //todo
            }
                , { error: Throwable ->
                    mView?.showProgress(false)
                    if (error is ServerException) {
                        mView?.showError(error.errorMessage)
                    } else {
                        mView?.showError(error.message)
                    }
                })
    }
}