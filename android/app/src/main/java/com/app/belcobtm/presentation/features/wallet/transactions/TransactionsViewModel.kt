package com.app.belcobtm.presentation.features.wallet.transactions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.GetTransactionListUseCase
import com.app.belcobtm.domain.wallet.interactor.GetBalanceUseCase
import com.app.belcobtm.domain.wallet.interactor.GetChartsUseCase
import com.app.belcobtm.domain.wallet.interactor.UpdateCoinFeeUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsScreenItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.mapToUiItem

class TransactionsViewModel(
    val coinCode: String,
    private val chartUseCase: GetChartsUseCase,
    private val transactionListUseCase: GetTransactionListUseCase,
    private val balanceUseCase: GetBalanceUseCase,
    private val updateCoinFeeUseCase: UpdateCoinFeeUseCase
) : ViewModel() {
    val chartLiveData: MutableLiveData<LoadingData<TransactionsScreenItem>> = MutableLiveData()
    val transactionListLiveData: MutableLiveData<List<TransactionsAdapterItem>> = MutableLiveData()
    val feeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    var currentChartPeriodType = ChartPeriodType.DAY
    var totalTransactionListSize: Int = 0
    var coinDataItemList: ArrayList<CoinDataItem>? = null
    var coinDataItem: CoinDataItem? = null

    init {
        updateData()
    }

    fun updateData() {
        chartLiveData.value = LoadingData.Loading()
        chartUseCase.invoke(
            params = GetChartsUseCase.Params(coinCode),
            onSuccess = { dataItem ->
                chartLiveData.value = LoadingData.Success(
                    TransactionsScreenItem(
                        balance = dataItem.balance,
                        priceUsd = dataItem.price,
                        chartDay = dataItem.chart.day,
                        chartWeek = dataItem.chart.week,
                        chartMonth = dataItem.chart.month,
                        chartThreeMonths = dataItem.chart.threeMonths,
                        chartYear = dataItem.chart.year
                    )
                )
            },
            onError = { chartLiveData.value = LoadingData.Error(it) }
        )
        updateCoinFeeUseCase.invoke(
            params = UpdateCoinFeeUseCase.Params(coinCode),
            onSuccess = {
                feeLiveData.value = LoadingData.Success(Unit)
            },
            onError = {
                feeLiveData.value = LoadingData.Error(it)
            }
        )
        refreshTransactionList()

        //Todo need find best way
        balanceUseCase.invoke(
            params = Unit,
            onSuccess = { dataItem ->
                coinDataItemList = ArrayList(dataItem.coinList)
                coinDataItem = dataItem.coinList.firstOrNull { it.code == coinCode }
            })
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
            onError = {}
        )
    }
}