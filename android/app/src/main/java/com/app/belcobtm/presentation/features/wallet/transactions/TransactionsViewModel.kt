package com.app.belcobtm.presentation.features.wallet.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.R
import com.app.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.app.belcobtm.data.websockets.base.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.transaction.interactor.GetTransactionListUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.app.belcobtm.domain.wallet.item.ChartChangesColor
import com.app.belcobtm.domain.wallet.item.ChartDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.transactions.item.CurrentChartInfo
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsScreenItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.mapToUiItem
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.collections.set

class TransactionsViewModel(
    val coinCode: String,
    private val walletObserver: WalletObserver,
    private val chartUseCase: GetChartsUseCase,
    private val transactionListUseCase: GetTransactionListUseCase,
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
        _loadingLiveData.value = LoadingData.Loading()
        subscribeToCoinDataItem(coinCode)
        updateData()
    }

    fun updateData() {
        loadChartData(PriceChartPeriod.PERIOD_DAY)
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
                _loadingLiveData.value = LoadingData.Success(Unit)
            },
            onError = {
                it.printStackTrace()
                _loadingLiveData.value = LoadingData.Error(it)
            }
        )
    }

    private fun subscribeToCoinDataItem(coinCode: String) {
        viewModelScope.launch {
            walletObserver.observe().receiveAsFlow()
                .filterIsInstance<WalletBalance.Balance>()
                .mapNotNull { balance -> balance.data.coinList.firstOrNull { it.code == coinCode } }
                .map { coinDataItem ->
                    TransactionsScreenItem(
                        balance = coinDataItem.balanceCoin,
                        priceUsd = coinDataItem.priceUsd,
                        reservedBalanceUsd = coinDataItem.reservedBalanceUsd,
                        reservedCode = coinDataItem.code,
                        reservedBalanceCoin = coinDataItem.reservedBalanceCoin
                    )
                }
                .onEach { screenItem -> _detailsLiveData.value = screenItem }
                .collect()
        }
    }
}