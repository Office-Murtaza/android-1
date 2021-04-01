package com.app.belcobtm.presentation.features.wallet.trade.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.PaymentOption
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.app.belcobtm.domain.trade.create.CreateTradeUseCase
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.livedata.TripleCombinedLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.parser.StringParser
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.create.model.CreateTradeItem

class CreateTradeViewModel(
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val createTradeUseCase: CreateTradeUseCase,
    private val checkTradeCreationAvailabilityUseCase: CheckTradeCreationAvailabilityUseCase,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>,
    private val amountFormatter: Formatter<Int>,
    private val amountParser: StringParser<Double>
) : ViewModel() {

    private lateinit var coinList: List<CoinDataItem>

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

    private val _amounRangeError = MutableLiveData<String?>()
    val amountRangeError: LiveData<String?> = _amounRangeError

    private val _priceError = MutableLiveData<String?>()
    val priceError: LiveData<String?> = _priceError

    private val _amountMinLimit = MutableLiveData<Int>()
    val amountMinLimit: LiveData<Int> = _amountMinLimit

    private val _amountMaxLimit = MutableLiveData<Int>()
    val amountMaxLimit: LiveData<Int> = _amountMaxLimit

    val cryptoAmountFormatted: LiveData<String> =
        TripleCombinedLiveData(price, amountMaxLimit, selectedCoin) { price, maxAmount, coin ->
            val cryptoAmount = if (maxAmount == null || price == null || price == 0.0) {
                0.0
            } else {
                maxAmount.toDouble() / price
            }
            stringProvider.getString(R.string.trade_crypto_amount_value, cryptoAmount, coin?.code.orEmpty())
        }

    init {
        fetchInitialData()
    }

    fun parseAmount(input: String) = amountParser.parse(input)

    fun formatAmount(amount: Int) = amountFormatter.format(amount)

    fun formatPrice(price: Double) = priceFormatter.format(price)

    fun fetchInitialData() {
        _initialLoadingData.value = LoadingData.Loading(Unit)
        getAvailableTradePaymentOptionsUseCase(Unit, onSuccess = { availablePaymentOptions ->
            _availablePaymentOptions.value = availablePaymentOptions
            getCoinListUseCase(
                params = Unit,
                onSuccess = { coinsDataList ->
                    coinList = coinsDataList
                    val coin = coinList.firstOrNull()
                    if (coin == null) {
                        _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                    } else {
                        _selectedCoin.value = coin
                        _initialLoadingData.value = LoadingData.Success(Unit)
                    }
                },
                onError = { _initialLoadingData.value = LoadingData.Error(Failure.ServerError()) }
            )
        }, onError = { _initialLoadingData.value = LoadingData.Error(Failure.ServerError()) })
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
        _selectedCoin.value = coinDataItem
    }

    fun createTrade(
        @TradeType type: Int,
        terms: String,
        minRangeAmount: Int,
        maxRangeAmount: Int
    ) {
        val paymentOptions = availablePaymentOptions.value.orEmpty()
            .asSequence()
            .filter(AvailableTradePaymentOption::selected)
            .map { it.payment.paymentId }
            .toList()
        if (paymentOptions.isEmpty()) {
            _snackbarMessage.value = stringProvider.getString(R.string.create_trade_no_payment_options_selected_error)
            return
        }
        val price = price.value ?: 0.0
        if (price <= 0.0) {
            _priceError.value = stringProvider.getString(R.string.create_trade_price_zero_error)
            return
        } else {
            _priceError.value = null
        }
        val fromAmount = _amountMinLimit.value ?: 0
        val toAmount = _amountMaxLimit.value ?: 0
        if (
            fromAmount < minRangeAmount || fromAmount > maxRangeAmount
            || toAmount < minRangeAmount || toAmount > maxRangeAmount ||
            toAmount < fromAmount
        ) {
            _amounRangeError.value = stringProvider.getString(
                R.string.create_trade_amount_range_error,
                amountFormatter.format(minRangeAmount),
                amountFormatter.format(maxRangeAmount)
            )
            return
        } else {
            _amounRangeError.value = null
        }
        val cryptoAmount = toAmount / price
        if (type == TradeType.SELL && cryptoAmount > selectedCoin.value?.reservedBalanceCoin ?: 0.0) {
            _snackbarMessage.value = stringProvider.getString(R.string.create_trade_not_enough_crypto_balance)
            return
        }
        val coinCode = selectedCoin.value?.code.orEmpty()
        checkTradeCreationAvailabilityUseCase(
            CheckTradeCreationAvailabilityUseCase.Params(coinCode, type),
            onSuccess = { canCreateTrade ->
                if (canCreateTrade) {
                    createTrade(type, coinCode, price, fromAmount, toAmount, terms, paymentOptions)
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
        fromAmount: Int,
        toAmount: Int,
        terms: String,
        paymentOptions: List<@PaymentOption Int>
    ) {
        _createTradeLoadingData.value = LoadingData.Loading()
        createTradeUseCase(
            CreateTradeItem(
                type, coinCode, price.toInt(),
                fromAmount, toAmount,
                terms, paymentOptions
            ), onSuccess = {
                _createTradeLoadingData.value = LoadingData.Success(it)
            }, onError = {
                _createTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }
}