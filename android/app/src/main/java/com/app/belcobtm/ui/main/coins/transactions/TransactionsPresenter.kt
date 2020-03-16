package com.app.belcobtm.ui.main.coins.transactions

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.pref


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


                mView?.setChart(ChartPeriodType.DAY, day)
                mView?.setChanges(changesPositive)
            },
            { error ->
                checkError(error)
            })
    }

    override fun chartButtonClicked(chartType: ChartPeriodType) {
        when (chartType) {
            ChartPeriodType.DAY -> mView?.setChart(chartType, day)
            ChartPeriodType.WEEK -> mView?.setChart(chartType, week)
            ChartPeriodType.MONTH -> mView?.setChart(chartType, month)
            ChartPeriodType.THREE_MONTHS -> mView?.setChart(chartType, threeMonths)
            ChartPeriodType.YEAR -> mView?.setChart(chartType, year)
        }

        when (chartType) {
            ChartPeriodType.DAY -> mView?.setChanges(changesPositive)
            ChartPeriodType.WEEK -> mView?.setChanges(changesNegative)
            ChartPeriodType.MONTH -> mView?.setChanges(changesNegative)
            ChartPeriodType.THREE_MONTHS -> mView?.setChanges(changesPositive)
            ChartPeriodType.YEAR -> mView?.setChanges(changesNegative)
        }
    }

    val day: List<Double> = listOf(995.0, 997.0, 1004.0, 1002.0, 1010.0, 1000.0, 990.0)
    val week: List<Double> = listOf(999.0, 1005.0, 990.0, 995.0, 1004.0, 1002.0, 1010.0)
    val month: List<Double> = listOf(995.0, 997.0, 1004.0, 1002.0, 1010.0, 1000.0, 990.0)
    val threeMonths: List<Double> = listOf(999.0, 990.0, 995.0, 997.0, 1002.0, 1010.0, 990.0)
    val year: List<Double> = listOf(1000.0, 999.0, 1005.0, 990.0, 995.0, 997.0)

    val changesPositive = 2.13
    val changesNegative = -10.44
}