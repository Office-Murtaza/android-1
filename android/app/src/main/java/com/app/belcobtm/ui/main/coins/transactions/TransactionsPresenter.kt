package com.app.belcobtm.ui.main.coins.transactions

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.core.pref


class TransactionsPresenter : BaseMvpDIPresenterImpl<TransactionsContract.View, CoinsDataManager>(),
    TransactionsContract.Presenter {
    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override val transactionList: ArrayList<TransactionModel> = arrayListOf()
    override var coinId: String = ""
    private var mTotalTransactions: Int = -1

    override fun getFirstTransactions() {
        transactionList.clear()
        getTransactions()
    }

    override fun getTransactions() {

        if (mTotalTransactions != -1 && mTotalTransactions == transactionList.size) {
            mView?.showProgress(false)
            return
        }

        val userId = App.appContext().pref.getUserId().toString()
        mDataManager.getTransactions(userId, coinId, transactionList.size + 1).subscribe(
            { response ->
                mView?.showProgress(false)
                if (response.value != null) {
                    mTotalTransactions = response.value!!.total
                    transactionList.addAll(response.value!!.transactions)
                }
                mView?.notifyTransactions()

            },
            { error ->
                checkError(error)
            })
    }
}