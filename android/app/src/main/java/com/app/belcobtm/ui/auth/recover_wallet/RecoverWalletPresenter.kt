package com.app.belcobtm.ui.auth.recover_wallet

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.AuthDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.api.model.response.AuthResponse
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.util.Optional
import com.app.belcobtm.util.pref
import io.reactivex.Observable


class RecoverWalletPresenter : BaseMvpDIPresenterImpl<RecoverWalletContract.View, AuthDataManager>(),
    RecoverWalletContract.Presenter {


    private var userId: String = ""
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override fun attemptRecover(phone: String, pass: String) {
        if (phone.isEmpty() || pass.isEmpty()) {
            mView?.showError(com.app.belcobtm.R.string.error_all_fields_required)
        } else if (pass.length < 4) {
            mView?.showError(com.app.belcobtm.R.string.error_short_pass)
        } else {
            mView?.showProgress(true)
            mDataManager.recoverWallet(phone, pass)
                .flatMap { response ->
                    App.appContext().pref.setSessionApiToken(response.value?.accessToken)
                    App.appContext().pref.setRefreshApiToken(response.value?.refreshToken)
                    App.appContext().pref.setUserId(response.value?.userId)
                    mDataManager.updateToken()

                    return@flatMap Observable.just(response)
                }
                .subscribe({ response: Optional<AuthResponse> ->
                    mView?.openSmsCodeDialog()
                    mView?.showProgress(false)
                    userId = response.value?.userId.toString()
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

    override fun verifyCode(code: String) {
        mView?.showProgress(true)
        mDataManager.verifySmsCode(userId,code)
            .subscribe({
                mView?.showProgress(false)
                mView?.onSmsSuccess()
            }
                , { error: Throwable ->
                    mView?.showProgress(false)
                    if (error is ServerException) {
                        mView?.openSmsCodeDialog(error.errorMessage)
                    } else {
                        mView?.showError(error.message)
                    }

                })
    }
}