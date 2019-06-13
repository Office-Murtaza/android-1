package com.app.belcobtm.mvp


abstract class BaseMvpPresenterImpl<V : BaseMvpView> : BaseMvpPresenter<V> {

    protected var mView: V? = null

    override fun attachView(view: V) {
        mView = view
    }

    override fun detachView() {
        mView = null
    }

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