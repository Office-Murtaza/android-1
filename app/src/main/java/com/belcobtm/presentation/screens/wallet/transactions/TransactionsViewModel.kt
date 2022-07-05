package com.belcobtm.presentation.screens.wallet.transactions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.belcobtm.presentation.screens.wallet.transactions.item.CurrentChartInfo
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsAdapterItem
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsScreenItem
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
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

    private val _transactionListLiveData = MutableLiveData<List<TransactionsAdapterItem>>()
    val transactionListLiveData: LiveData<List<TransactionsAdapterItem>> = _transactionListLiveData

    private val chartInfo = HashMap<PriceChartPeriod, ChartDataItem>()

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
        subscribeOnTransactions()
        updateData()
    }

    private fun subscribeOnTransactions() {
        observeTransactionsUseCase.invoke(coinCode).onEach {
            _transactionListLiveData.value = it
        }.launchIn(viewModelScope)
    }

    fun updateData() {
        loadChartData(
            period = PriceChartPeriod.DAY,
            isInit = true
        )
    }

    fun changeCurrentTypePeriod(selectedPeriod: Int) {
        val period = when (selectedPeriod) {
            R.id.one_day_chip_view -> PriceChartPeriod.DAY
            R.id.one_week_chip_view -> PriceChartPeriod.WEEK
            R.id.one_month_chip_view -> PriceChartPeriod.MONTH
            R.id.three_month_chip_view -> PriceChartPeriod.MONTH_3
            R.id.one_year_chip_view -> PriceChartPeriod.YEAR
            else -> return
        }
        chartInfo[period]?.let {
            chartLiveData.value = LoadingData.Success(CurrentChartInfo(period, it))
        } ?: loadChartData(period)
    }

    private fun loadChartData(period: PriceChartPeriod, isInit: Boolean = false) {
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

                if (isInit) fetchTransactions()
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

    fun fetchTransactions() {
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
