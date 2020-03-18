package com.app.belcobtm.ui.main.coins.transactions

import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import com.app.belcobtm.presentation.core.pref


class TransactionsPresenter : BaseMvpDIPresenterImpl<TransactionsContract.View, CoinsDataManager>(),
    TransactionsContract.Presenter {

    private var balance: Double = 0.0
    private var price: Double = 0.0
    private var chartDay: Pair<Double, List<Double>> = Pair(0.0, emptyList())
    private var chartWeek: Pair<Double, List<Double>> = Pair(0.0, emptyList())
    private var chartMonth: Pair<Double, List<Double>> = Pair(0.0, emptyList())
    private var chartThreeMonths: Pair<Double, List<Double>> = Pair(0.0, emptyList())
    private var chartYear: Pair<Double, List<Double>> = Pair(0.0, emptyList())
    private var currentChartPeriodType = ChartPeriodType.DAY

    override fun injectDependency() {
        presenterComponent.inject(this)
    }

    override val transactionList: ArrayList<TransactionModel> = arrayListOf()
    override var coinId: String = ""
    private var mTotalTransactions: Int = -1

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

    override fun getFirstTransactions() {
        transactionList.clear()
        getTransactions()
    }

    override fun chartViewInitialized() {
        val userId = App.appContext().pref.getUserId().toString()
        mDataManager.getChart(userId).subscribe(
            {
                val chart = it.value?.chart
                balance = it.value?.balance ?: 0.0
                price = it.value?.price ?: 0.0
                chartDay = Pair(chart?.day?.changes ?: 0.0, chart?.day?.prices ?: emptyList())
                chartWeek = Pair(chart?.week?.changes ?: 0.0, chart?.week?.prices ?: emptyList())
                chartMonth = Pair(chart?.month?.changes ?: 0.0, chart?.month?.prices ?: emptyList())
                chartThreeMonths = Pair(chart?.threeMonths?.changes ?: 0.0, chart?.threeMonths?.prices ?: emptyList())
                chartYear = Pair(chart?.year?.changes ?: 0.0, chart?.year?.prices ?: emptyList())

                mView?.setBalance(balance)
                mView?.setPrice(price)
                updateChartByPeriod(currentChartPeriodType)
            },
            { checkError(it) }
        )
    }

    override fun chartButtonClicked(chartType: ChartPeriodType) {
        updateChartByPeriod(chartType)
    }

    private fun updateChartByPeriod(chartType: ChartPeriodType) {
        currentChartPeriodType = chartType
        when (chartType) {
            ChartPeriodType.DAY -> mView?.setChart(chartType, chartDay.second)
            ChartPeriodType.WEEK -> mView?.setChart(chartType, chartWeek.second)
            ChartPeriodType.MONTH -> mView?.setChart(chartType, chartMonth.second)
            ChartPeriodType.THREE_MONTHS -> mView?.setChart(chartType, chartThreeMonths.second)
            ChartPeriodType.YEAR -> mView?.setChart(chartType, chartYear.second)
        }

        when (chartType) {
            ChartPeriodType.DAY -> mView?.setChanges(chartDay.first)
            ChartPeriodType.WEEK -> mView?.setChanges(chartWeek.first)
            ChartPeriodType.MONTH -> mView?.setChanges(chartMonth.first)
            ChartPeriodType.THREE_MONTHS -> mView?.setChanges(chartThreeMonths.first)
            ChartPeriodType.YEAR -> mView?.setChanges(chartYear.first)
        }
    }


}