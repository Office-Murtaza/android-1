package com.app.belcobtm.presentation.features.wallet.transactions

import android.preference.PreferenceManager
import com.app.belcobtm.App
import com.app.belcobtm.api.data_manager.CoinsDataManager
import com.app.belcobtm.api.model.response.ChartResponse
import com.app.belcobtm.api.model.response.GetTransactionsResponse
import com.app.belcobtm.api.model.response.TransactionModel
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.data.rest.wallet.response.GetCoinFeeResponse
import com.app.belcobtm.data.rest.wallet.response.mapToDataItem
import com.app.belcobtm.mvp.BaseMvpDIPresenterImpl
import io.reactivex.disposables.CompositeDisposable

class TransactionsPresenter : BaseMvpDIPresenterImpl<TransactionsContract.View, CoinsDataManager>(),
    TransactionsContract.Presenter {

    //TODO need migrate to dependency koin after refactoring
    private val prefsHelper: SharedPreferencesHelper by lazy {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.appContext())
        SharedPreferencesHelper(sharedPreferences)
    }
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
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

    override fun viewCreated() {
        initializeData()
    }

    override fun viewDestroyed() {
        compositeDisposable.dispose()
    }

    override fun chartButtonClicked(chartType: ChartPeriodType) {
        updateChartByPeriod(chartType)
    }

    override fun refreshTransactionClicked() {
        transactionList.clear()
        downloadTransactionList(0)
    }

    override fun scrolledToLastTransactionItem() {
        if (mTotalTransactions != -1 && mTotalTransactions == transactionList.size) {
            mView?.showProgress(false)
        } else {
            downloadTransactionList(transactionList.size + 1)
        }
    }

    private fun initializeData() {
//        val userId = prefsHelper.userId.toString()
//        val mergeList = mutableListOf(
//            mDataManager.getChart(userId, coinId),
//            mDataManager.getTransactions(userId, coinId, 0)
//        )
//
//        if (prefsHelper.coinsFee[coinId] == null) {
//            mergeList.add(mDataManager.getCoinFee(coinId))
//        }
//
//        val requestData = Observable.merge(mergeList).subscribe(
//            { optional ->
//                when (val response = optional.value) {
//                    is ChartResponse -> chartDownloaded(response)
//                    is GetTransactionsResponse -> {
//                        transactionsDownloaded(response)
//                    }
//                    is GetCoinFeeResponse -> coinFeeDownloaded(response)
//                }
//            },
//            { checkError(it) },
//            { mView?.showProgress(false) }
//        )
//        compositeDisposable.add(requestData)
    }

    private fun downloadTransactionList(currentListSize: Int) {
        val request = mDataManager.getTransactions(prefsHelper.userId.toString(), coinId, currentListSize).subscribe(
            { response ->
                response.value?.let { transactionsDownloaded(it) }
                mView?.showProgress(false)
            },
            { checkError(it) }
        )

        compositeDisposable.add(request)
    }

    private fun chartDownloaded(response: ChartResponse) {
        val chart = response.chart
        balance = response.balance
        price = response.price
        chartDay = Pair(chart.day.changes, chart.day.prices)
        chartWeek = Pair(chart.week.changes, chart.week.prices)
        chartMonth = Pair(chart.month.changes, chart.month.prices)
        chartThreeMonths = Pair(chart.threeMonths.changes, chart.threeMonths.prices)
        chartYear = Pair(chart.year.changes, chart.year.prices)
        mView?.setBalance(balance)
        mView?.setPrice(price)
        updateChartByPeriod(currentChartPeriodType)
    }

    private fun transactionsDownloaded(response: GetTransactionsResponse) {
        mTotalTransactions = response.total
        val newItems = if (transactionList.isEmpty()) {
            response.transactions
        } else {
            response.transactions.filter { responseItem ->
                transactionList.firstOrNull { responseItem.txid == it.txid } == null
            }
        }
        transactionList.addAll(newItems)
        mView?.notifyTransactions()
    }

    private fun coinFeeDownloaded(response: GetCoinFeeResponse) {
        val mutableCoinsFeeMap = prefsHelper.coinsFee.toMutableMap()
        mutableCoinsFeeMap[coinId] = response.mapToDataItem()
        prefsHelper.coinsFee = mutableCoinsFeeMap
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