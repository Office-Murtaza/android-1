package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.R
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.app.belcobtm.domain.trade.create.CreateTradeUseCase
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetFreshCoinsUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem
import com.app.belcobtm.presentation.core.livedata.CombinedLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem
import kotlinx.coroutines.launch

class CreateTradeViewModel(
    private val getCoinDetailsUseCase: GetCoinDetailsUseCase,
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getFreshCoinsUseCase: GetFreshCoinsUseCase,
    private val accountDao: AccountDao,
    private val createTradeUseCase: CreateTradeUseCase,
    private val checkTradeCreationAvailabilityUseCase: CheckTradeCreationAvailabilityUseCase,
    private val stringProvider: StringProvider
) : ViewModel() {

    private lateinit var coinList: List<CoinDataItem>
    private lateinit var coinDetails: CoinDetailsDataItem

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _createTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val createTradeLoadingData: LiveData<LoadingData<Unit>> = _createTradeLoadingData

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?> = _cryptoAmountError

    private val _price = MutableLiveData<Double>(0.0)
    val price: LiveData<Double> = _price

    private val _availablePaymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val availablePaymentOptions: LiveData<List<AvailableTradePaymentOption>> = _availablePaymentOptions

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _priceRangeError = MutableLiveData<String?>()
    val priceRangeError: LiveData<String?> = _priceRangeError

    private val _priceError = MutableLiveData<String?>()
    val priceError: LiveData<String?> = _priceError

    private val _amountMinLimit = MutableLiveData<Int>()
    val amountMinLimit: LiveData<Int> = _amountMinLimit

    private val _amountMaxLimit = MutableLiveData<Int>()
    val amountMaxLimit: LiveData<Int> = _amountMaxLimit

    val cryptoAmountFormatted: LiveData<String> = CombinedLiveData(price, amountMaxLimit) { price, maxAmount ->
        val cryptoAmount = if (maxAmount == null || price == null || price == 0.0) {
            0.0
        } else {
            maxAmount.toDouble() / price
        }
        stringProvider.getString(R.string.trade_crypto_amount_value, cryptoAmount, selectedCoin.value?.code)
    }

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

    fun updateMinAmount(amount: Int) {
        _amountMinLimit.value = amount
    }

    fun updateMaxAmount(amount: Int) {
        _amountMaxLimit.value = amount
    }

    fun selectCoin(coinDataItem: CoinDataItem) {
        _initialLoadingData.value = LoadingData.Loading()
        updateCoinInfo(coinDataItem)
    }

    fun createTrade(@TradeType type: Int, priceRange: List<Float>, terms: String) {
        // TODO validate unique trade
        val paymentOptions = availablePaymentOptions.value.orEmpty()
            .asSequence()
            .filter(AvailableTradePaymentOption::selected)
            .map { it.payment.paymentId }
            .toList()
        val price = price.value ?: 0.0
        if (price <= 0.0) {
            _priceError.value = stringProvider.getString(R.string.create_trade_price_zero_error)
            return
        } else {
            _priceError.value = null
        }
        if (paymentOptions.isEmpty()) {
            _snackbarMessage.value = stringProvider.getString(R.string.create_trade_no_payment_options_selected_error)
            return
        }
        if (priceRange[0] <= 0.0f || priceRange[1] <= 0.0f) {
            _priceRangeError.value = stringProvider.getString(R.string.create_trade_price_range_zero_error)
            return
        } else {
            _priceRangeError.value = null
        }
        val cryptoAmount = priceRange[1] / price
        if (type == TradeType.SELL && cryptoAmount > selectedCoin.value?.reservedBalanceCoin ?: 0.0) {
            _snackbarMessage.value = stringProvider.getString(R.string.create_trade_not_enough_crypto_balance)
            return
        }
        val coinCode = selectedCoin.value?.code.orEmpty()
        checkTradeCreationAvailabilityUseCase(
            CheckTradeCreationAvailabilityUseCase.Params(coinCode, type),
            onSuccess = { canCreateTrade ->
                if (canCreateTrade) {
                    createTrade(type, coinCode, price, priceRange, terms, paymentOptions)
                } else {
                    val tradeLabel = stringProvider.getString(
                        if (type == TradeType.SELL) {
                            R.string.trade_type_sell_label
                        } else {
                            R.string.trade_type_buy_label
                        }
                    )
                    _snackbarMessage.value = stringProvider.getString(
                        R.string.create_trade_already_exists, coinCode, tradeLabel
                    )
                    _createTradeLoadingData.value = LoadingData.Error(Failure.ClientValidationError())
                }
            },
            onError = {
                _createTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }

    private fun createTrade(
        type: Int,
        coinCode: String,
        price: Double,
        priceRange: List<Float>,
        terms: String,
        paymentOptions: List<@PaymentOption Int>
    ) {
        _createTradeLoadingData.value = LoadingData.Loading()
        createTradeUseCase.invoke(
            CreateTradeItem(
                type, coinCode, price.toInt(),
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
                this.coinDetails = coinDetails
                _initialLoadingData.value = LoadingData.Success(Unit)
            },
            onError = {
                _initialLoadingData.value = LoadingData.Error(it)
            }
        )
    }
}