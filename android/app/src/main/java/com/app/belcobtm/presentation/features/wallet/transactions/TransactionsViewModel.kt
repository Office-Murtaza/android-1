package com.app.belcobtm.presentation.features.wallet.transactions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.GetTransactionListUseCase
import com.app.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import com.app.belcobtm.domain.wallet.interactor.UpdateCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsScreenItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.mapToUiItem

class TransactionsViewModel(
    val coinCode: String,
    private val chartUseCase: GetChartsUseCase,
    private val transactionListUseCase: GetTransactionListUseCase,
    private val updateCoinDetailsUseCase: UpdateCoinDetailsUseCase,
    private val getCoinByCodeUseCase: GetCoinByCodeUseCase
) : ViewModel() {
    val chartLiveData: MutableLiveData<TransactionsScreenItem> = MutableLiveData()
    val detailsLiveData: MutableLiveData<LoadingData<CoinDetailsDataItem>> = MutableLiveData()
    val transactionListLiveData: MutableLiveData<List<TransactionsAdapterItem>> = MutableLiveData()
    var currentChartPeriodType = ChartPeriodType.DAY
    var totalTransactionListSize: Int = 0

    init {
        updateData()
    }

    fun updateData() {
        detailsLiveData.value = LoadingData.Loading()
        chartUseCase.invoke(
            params = GetChartsUseCase.Params(coinCode),
            onSuccess = { dataItem ->
                val coindDataItem = getCoinByCodeUseCase.invoke(coinCode)
                chartLiveData.value = TransactionsScreenItem(
                    balance = dataItem.balance,
                    priceUsd = dataItem.price,
                    reservedBalanceUsd = coindDataItem.reservedBalanceUsd,
                    reservedCode = coindDataItem.code,
                    reservedBalanceCoin = coindDataItem.reservedBalanceCoin,
                    chartDay = dataItem.chart.day,
                    chartWeek = dataItem.chart.week,
                    chartMonth = dataItem.chart.month,
                    chartThreeMonths = dataItem.chart.threeMonths,
                    chartYear = dataItem.chart.year
                )
            },
            onError = { it.printStackTrace() }
        )
        updateCoinDetailsUseCase.invoke(
            params = UpdateCoinDetailsUseCase.Params(coinCode),
            onSuccess = { detailsLiveData.value = LoadingData.Success(it) },
            onError = { detailsLiveData.value = LoadingData.Error(it) }
        )
        refreshTransactionList()
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
}