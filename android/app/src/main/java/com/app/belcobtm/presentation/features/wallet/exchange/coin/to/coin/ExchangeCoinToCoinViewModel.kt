package com.app.belcobtm.presentation.features.wallet.exchange.coin.to.coin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.CoinToCoinExchangeUseCase
import com.app.belcobtm.domain.transaction.interactor.CreateTransactionUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinFeeDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class ExchangeCoinToCoinViewModel(
    val fromCoinFeeItem: CoinFeeDataItem,
    val fromCoinItem: CoinDataItem,
    val coinItemList: List<CoinDataItem>,
    val coinFeeItemList: Map<String, CoinFeeDataItem>,
    private val exchangeUseCase: CoinToCoinExchangeUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {
    private var transactionHash: String = ""
    val exchangeLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()
    var toCoinItem: CoinDataItem? = coinItemList.find { it.code == LocalCoinType.BTC.name }
    var toCoinFeeItem: CoinFeeDataItem? = coinFeeItemList[LocalCoinType.BTC.name]

    fun createTransaction(fromCoinAmount: Double) {
        exchangeLiveData.value = LoadingData.Loading()
        createTransactionUseCase.invoke(
            CreateTransactionUseCase.Params(fromCoinItem.code, fromCoinAmount),
            onSuccess = { hash ->
                transactionHash = hash
                exchangeLiveData.value = LoadingData.Success(TRANSACTION_CREATED)
            },
            onError = { exchangeLiveData.value = LoadingData.Error(it, TRANSACTION_CREATED) }
        )
    }

    fun exchangeTransaction(
        code: String,
        amountFromCoin: Double
    ) {
        exchangeLiveData.value = LoadingData.Loading()
        exchangeUseCase.invoke(
            CoinToCoinExchangeUseCase.Params(
                code,
                amountFromCoin,
                fromCoinItem.code,
                toCoinItem?.code ?: "",
                transactionHash
            ),
            onSuccess = { exchangeLiveData.value = LoadingData.Success(TRANSACTION_EXCHANGED) },
            onError = { exchangeLiveData.value = LoadingData.Error(it, TRANSACTION_EXCHANGED) }
        )
    }

    companion object {
        const val TRANSACTION_CREATED = "transaction_created"
        const val TRANSACTION_EXCHANGED = "transaction_exchanged"
    }
}