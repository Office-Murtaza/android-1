package com.belcobtm.presentation.features.wallet.transactions

import androidx.lifecycle.*
import com.belcobtm.R
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.toDataItem
import com.belcobtm.data.rest.wallet.request.PriceChartPeriod
import com.belcobtm.domain.transaction.interactor.FetchTransactionsUseCase
import com.belcobtm.domain.transaction.interactor.ObserveTransactionsUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.belcobtm.domain.wallet.item.ChartChangesColor
import com.belcobtm.domain.wallet.item.ChartDataItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.wallet.transactions.item.CurrentChartInfo
import com.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.belcobtm.presentation.features.wallet.transactions.item.TransactionsScreenItem
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.collections.set

class TransactionsViewModel(
    val coinCode: String,
    private val walletDao: WalletDao,
    private val chartUseCase: GetChartsUseCase,
    private val transactionsUseCase: FetchTransactionsUseCase,
    private val observeTransactionsUseCase: ObserveTransactionsUseCase
) : ViewModel() {

    val chartLiveData: MutableLiveData<LoadingData<CurrentChartInfo>> =
        MutableLiveData(LoadingData.Loading())

    val transactionListLiveData: LiveData<List<TransactionsAdapterItem>>
        get() = observeTransactionsUseCase.invoke(coinCode).asLiveData()
    private val chartInfo = HashMap<@PriceChartPeriod Int, ChartDataItem>()

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
        fetchTransactions()
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

    private fun fetchTransactions() {
        transactionsUseCase.invoke(
            FetchTransactionsUseCase.Params(coinCode),
            onSuccess = {
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
            walletDao.observeCoins()
                .mapNotNull { entity ->
                    entity.firstOrNull { it.coin.code == coinCode }?.toDataItem()
                }
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