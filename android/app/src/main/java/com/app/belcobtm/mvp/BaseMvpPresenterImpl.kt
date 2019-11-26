package com.app.belcobtm.mvp

import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.util.Const


abstract class BaseMvpPresenterImpl<V : BaseMvpView> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }


    protected fun <T : Throwable> onError(exception: T) {
        mView?.showError(exception.message)
    }

    protected fun checkError(error: Throwable) {
        mView?.showProgress(false)
        if (error is ServerException) {
            if (error.code == Const.ERROR_403) {
                mView?.onRefreshTokenFailed()
            } else {
                mView?.showError(error.errorMessage)
            }
        } else {
            mView?.showError(error.message)
        }
    }

}