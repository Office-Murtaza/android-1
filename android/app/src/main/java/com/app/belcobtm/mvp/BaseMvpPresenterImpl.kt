package com.app.belcobtm.mvp

import com.app.belcobtm.api.data_manager.BaseDataManager


abstract class BaseMvpPresenterImpl<V : BaseMvpView, T : BaseDataManager> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

    protected abstract var mDataManager: T


    protected fun <T : Throwable> onError(exception: T) {
        //TODO add error handler logic in this api
//        if (exception is ServerException) {
//            val messageStringId = exception.getMessageStringId()
//            if(messageStringId == -1)
//                mView?.showError(exception.message)
//            else
//                mView?.showError(messageStringId)
//        } else
        mView?.showError(exception.message)
    }

}