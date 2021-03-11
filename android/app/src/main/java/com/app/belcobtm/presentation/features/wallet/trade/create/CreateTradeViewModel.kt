package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.create.CreateTradeUseCase
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.coin.AmountCoinValidator
import com.app.belcobtm.presentation.core.coin.MinMaxCoinValueProvider
import com.app.belcobtm.presentation.core.extensions.toStringUsd
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import kotlinx.coroutines.launch

class CreateTradeViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getFreshCoinsUseCase: GetFreshCoinsUseCase,
    private val accountDao: AccountDao,
    private val createTradeUseCase: CreateTradeUseCase,
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

    private val _availablePaymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val availablePaymentOptions: LiveData<List<AvailableTradePaymentOption>> = _availablePaymentOptions

    private val _snackbarMessage = MutableLiveData<@StringRes Int>()
    val snackbarMessage: LiveData<Int> = _snackbarMessage

    private val _priceRangeError = MutableLiveData<@StringRes Int?>()
    val priceRangeError: LiveData<Int?> = _priceRangeError

    private val _priceError = MutableLiveData<@StringRes Int?>()
    val priceError: LiveData<Int?> = _priceError

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        viewModelScope.launch {
            _initialLoadingData.value = LoadingData.Loading(Unit)
            val allCoins = accountDao.getItemList().orEmpty()
            getAvailableTradePaymentOptionsUseCase.invoke(Unit, onSuccess = { availablePaymentOptions ->
                _availablePaymentOptions.value = availablePaymentOptions
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
            }, onError = { _initialLoadingData.value = LoadingData.Error(Failure.ServerError()) })
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

    fun createTrade(@TradeType type: Int, priceRange: List<Float>, terms: String) {
        // TODO validate range for buy
        // TODO validate unique trade
        val paymentOptions = availablePaymentOptions.value.orEmpty()
            .asSequence()
            .filter(AvailableTradePaymentOption::selected)
            .map { it.payment.paymentId }
            .toList()
        val price = price.value ?: 0.0
        if (price <= 0.0) {
            _priceError.value = R.string.create_trade_price_zero_error
            return
        } else {
            _priceError.value = null
        }
        if (paymentOptions.isEmpty()) {
            _snackbarMessage.value = R.string.create_trade_no_payment_options_selected_error
            return
        }
        if (priceRange[0] <= 0.0f || priceRange[1] <= 0.0f) {
            _priceRangeError.value = R.string.create_trade_price_range_zero_error
            return
        } else {
            _priceRangeError.value = null
        }
        _createTradeLoadingData.value = LoadingData.Loading()
        createTradeUseCase.invoke(
            CreateTradeItem(
                type, selectedCoin.value?.code.orEmpty(), price.toInt(),
                priceRange[0].toInt(), priceRange[1].toInt(),
                terms, paymentOptions
            ), onSuccess = {
                _createTradeLoadingData.value = LoadingData.Success(it)
            }, onError = {
                _createTradeLoadingData.value = LoadingData.Error(it)
            }
        )
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