package com.app.belcobtm.presentation.features.wallet.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.domain.transaction.interactor.GetTransactionListUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.interactor.UpdateCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.item.ChartChangesColor
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.transactions.item.CurrentChartInfo
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsScreenItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.mapToUiItem
import com.github.mikephil.charting.data.BarEntry
import kotlin.collections.set

class TransactionsViewModel(
    val coinCode: String,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase,
    private val chartUseCase: GetChartsUseCase,
    private val transactionListUseCase: GetTransactionListUseCase,
    private val updateCoinDetailsUseCase: UpdateCoinDetailsUseCase
) : ViewModel() {
    val chartLiveData: MutableLiveData<LoadingData<CurrentChartInfo>> =
        MutableLiveData(LoadingData.Loading())
    val transactionListLiveData: MutableLiveData<List<TransactionsAdapterItem>> = MutableLiveData()
    private val chartInfo = HashMap<@PriceChartPeriod Int, ChartDataItem>()
    var totalTransactionListSize: Int = 0

    private val _detailsLiveData = MutableLiveData<TransactionsScreenItem>()
    val detailsLiveData: LiveData<TransactionsScreenItem> = _detailsLiveData

    private val _loadingLiveData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>>
        get() = _loadingLiveData

    companion object {
        const val CATM_PRICE = 0.1
        const val CATM_CHANGES = 0.0
    }

    init {
        loadCoinDataItem(coinCode)
        updateData()
    }

    fun updateData() {
        _loadingLiveData.value = LoadingData.Loading()
        loadChartData(PriceChartPeriod.PERIOD_DAY)
        updateCoinDetailsUseCase.invoke(
            params = UpdateCoinDetailsUseCase.Params(coinCode),
            onSuccess = {
                _loadingLiveData.value = LoadingData.Success(Unit)
            },
            onError = {
                _loadingLiveData.value = LoadingData.Error(it)
            }
        )
        refreshTransactionList()
    }

    fun changeCurrentTypePeriod(selectedPeriod: Int) {
        val period = when (selectedPeriod) {
            R.id.one_week_chip_view -> PriceChartPeriod.PERIOD_WEEK
            R.id.one_month_chip_view -> PriceChartPeriod.PERIOD_MONTH
            R.id.three_month_chip_view -> PriceChartPeriod.PERIOD_QUARTER
            R.id.one_year_chip_view -> PriceChartPeriod.PERIOD_YEAR
            R.id.one_day_chip_view -> PriceChartPeriod.PERIOD_DAY
            else -> return
        }
        chartInfo[period]?.let {
            chartLiveData.value = LoadingData.Success(CurrentChartInfo(period, it))
        } ?: loadChartData(period)
    }

    private fun loadChartData(@PriceChartPeriod period: Int) {
        if (coinCode == LocalCoinType.CATM.name) {
            val catmChartStubEnd = BarEntry(10.0f, CATM_PRICE.toFloat())
            val catmChartStubStart = BarEntry(0.0f, CATM_PRICE.toFloat())
            val data = CurrentChartInfo(
                period, ChartDataItem(
                    ChartChangesColor.BLACK,
                    CATM_CHANGES,
                    listOf(catmChartStubStart, catmChartStubEnd),
                    listOf(catmChartStubEnd)
                )
            )
            chartLiveData.value = LoadingData.Success(data)
            return
        }
        chartLiveData.value = LoadingData.Loading()
        chartUseCase.invoke(
            params = GetChartsUseCase.Params(coinCode, period),
            onSuccess = { dataItem ->
                chartInfo[period] = dataItem
                chartLiveData.value = LoadingData.Success(CurrentChartInfo(period, dataItem))
            },
            onError = { failure ->
                chartLiveData.value = LoadingData.Error(
                    failure, CurrentChartInfo(
                        period,
                        ChartDataItem(
                            ChartChangesColor.BLACK, 0.0, emptyList(), emptyList()
                        )
                    )
                )
            }
        )
    }

    fun updateTransactionList() {
        val transactionListSize = transactionListLiveData.value?.size ?: 0
        if (totalTransactionListSize != transactionListSize) {
            downloadTransactionList(transactionListSize + 1, false)
        }
    }

    fun refreshTransactionList() {
        downloadTransactionList(0, true)
    }

    private fun downloadTransactionList(currentListSize: Int, isNeedClearList: Boolean) {
        transactionListUseCase.invoke(
            GetTransactionListUseCase.Params(coinCode, currentListSize),
            onSuccess = { dataItem ->
                totalTransactionListSize = dataItem.first
                if (transactionListLiveData.value.isNullOrEmpty() || isNeedClearList) {
                    transactionListLiveData.value = dataItem.second.map { it.mapToUiItem() }
                } else {
                    val oldList = transactionListLiveData.value ?: mutableListOf()
                    val newList: MutableList<TransactionsAdapterItem> = mutableListOf()
                    newList.addAll(oldList)
                    newList.addAll(dataItem.second.filter { responseItem ->
                        oldList.firstOrNull { responseItem.txId == it.id } == null
                    }.map { it.mapToUiItem() })
                    transactionListLiveData.value = newList
                }
            },
            onError = { it.printStackTrace() }
        )
    }

    private fun loadCoinDataItem(coinCode: String) {
        getCoinByCodeUseCase(
            coinCode,
            onSuccess = { coinDataItem ->
                _detailsLiveData.value = TransactionsScreenItem(
                    balance = coinDataItem.balanceCoin,
                    priceUsd = coinDataItem.priceUsd,
                    reservedBalanceUsd = coinDataItem.reservedBalanceUsd,
                    reservedCode = coinDataItem.code,
                    reservedBalanceCoin = coinDataItem.reservedBalanceCoin
                )
            }
        )
    }
}