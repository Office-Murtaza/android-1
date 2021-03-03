package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class CreateTradeViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getFreshCoinsUseCase: GetFreshCoinsUseCase,
    private val accountDao: AccountDao,
    private val minMaxCoinValueProvider: MinMaxCoinValueProvider,
    private val amountCoinValidator: AmountCoinValidator,
) : ViewModel() {

    private lateinit var coinList: List<CoinDataItem>
    private lateinit var coinToSendDetailsDataItem: CoinDetailsDataItem

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _createTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val createTradeLoadingData: LiveData<LoadingData<Unit>> = _createTradeLoadingData

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _cryptoAmountError = MutableLiveData<@StringRes Int?>()
    val cryptoAmountError: LiveData<Int?> = _cryptoAmountError

    private val _price = MutableLiveData<Double>(0.0)
    val price: LiveData<Double> = _price

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getItemList().orEmpty()
            if (allCoins.isNotEmpty()) {
                val coinCodesList = allCoins.map { it.type.name }
                getFreshCoinsUseCase(
                    params = GetFreshCoinsUseCase.Params(coinCodesList),
                    onSuccess = { coinsDataList ->
                        coinList = coinsDataList
                        val coin = coinList.firstOrNull()
                        if (coin == null) {
                            _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                        } else {
                            updateCoinInfo(coin)
                        }
                    },
                    onError = { _initialLoadingData.value = LoadingData.Error(Failure.ServerError()) }
                )
            } else {
                _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
            }
        }
    }

    fun getCoinsToSelect(): List<CoinDataItem> =
        coinList.filter { selectedCoin.value?.code != it.code }

    fun updatePrice(amount: Double) {
        _price.value = amount
    }

    fun selectCoin(coinDataItem: CoinDataItem) {
        _initialLoadingData.value = LoadingData.Loading()
        updateCoinInfo(coinDataItem)
    }

    private fun updateCoinInfo(coinToSend: CoinDataItem) {
        getCoinDetailsUseCase(
            params = GetCoinDetailsUseCase.Params(coinToSend.code),
            onSuccess = { coinDetails ->
                _selectedCoin.value = coinToSend
                coinToSendDetailsDataItem = coinDetails
                _initialLoadingData.value = LoadingData.Success(Unit)
            },
            onError = {
                _initialLoadingData.value = LoadingData.Error(it)
            }
        )
    }

    private fun processCoinItem(liveData: MediatorLiveData<String>, cryptoAmount: Double?, coinData: CoinDataItem?) {
        if (cryptoAmount != null && coinData != null) {
            liveData.value = (cryptoAmount * coinData.priceUsd).toStringUsd()
        }
    }
}