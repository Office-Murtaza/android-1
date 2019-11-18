package com.app.belcobtm.mvp

import com.app.belcobtm.api.data_manager.BaseDataManager
import com.app.belcobtm.api.model.ServerException
import com.app.belcobtm.di.component.DaggerPresenterComponent
import com.app.belcobtm.di.component.PresenterComponent
import com.app.belcobtm.di.module.PresenterModule
import com.app.belcobtm.util.Const
import javax.inject.Inject


abstract class BaseMvpPresenterImpl<V : BaseMvpView> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }



    protected fun <T : Throwable> onError(exception: T) {
//        if (exception is ServerException) {
//            val messageStringId = exception.getMessageStringId()
//            if(messageStringId == -1)
//                mView?.showError(exception.message)
//            else
//                mView?.showError(messageStringId)
//        } else
        mView?.showError(exception.message)
    }

    protected fun checkError(error: Throwable) {
        mView?.showProgress(false)
        if (error is ServerException) {
            if(error.code == Const.ERROR_403){
                mView?.onRefreshTokenFailed()
            } else {
                mView?.showError(error.errorMessage)
            }
        } else {
            mView?.showError(error.message)
        }
    }

}