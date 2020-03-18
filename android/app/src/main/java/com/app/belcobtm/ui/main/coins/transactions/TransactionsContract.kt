package com.app.belcobtm.ui.main.coins.transactions

import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpPresenter
import com.app.belcobtm.mvp.BaseMvpView


object TransactionsContract {
    interface Presenter : BaseMvpPresenter<View> {
        val transactionList: ArrayList<TransactionModel>
        var coinId: String

        fun getTransactions()
        fun getFirstTransactions()
        fun chartViewInitialized()
        fun chartButtonClicked(chartType: ChartPeriodType)
    }

    interface View : BaseMvpView {
        fun notifyTransactions()
        fun setPrice(price: Double)
        fun setBalance(balance: Double)
        fun setChanges(changes: Double)
        fun setChart(chartType: ChartPeriodType, chartList: List<Double>)
    }
}