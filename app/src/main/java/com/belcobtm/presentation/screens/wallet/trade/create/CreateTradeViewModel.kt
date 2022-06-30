package com.belcobtm.presentation.screens.wallet.trade.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.model.trade.PaymentOption
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.trade.create.CheckTradeCreationAvailabilityUseCase
import com.belcobtm.domain.trade.create.CreateTradeUseCase
import com.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.interactor.UpdateReservedBalanceUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.tools.extensions.toStringCoin
import com.belcobtm.presentation.core.livedata.TripleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption
import com.belcobtm.presentation.screens.wallet.trade.create.model.CreateTradeItem

class CreateTradeViewModel(
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val createTradeUseCase: CreateTradeUseCase,
    private val checkTradeCreationAvailabilityUseCase: CheckTradeCreationAvailabilityUseCase,
    private val stringProvider: StringProvider,
    private val serviceInfoProvider: ServiceInfoProvider,
    private val updateReservedBalanceUseCase: UpdateReservedBalanceUseCase,
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

    private val _price = MutableLiveData(0.0)
    val price: LiveData<Double> = _price

    private val _availablePaymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val availablePaymentOptions: LiveData<List<AvailableTradePaymentOption>> =
        _availablePaymentOptions

    private val _amountRangeError = MutableLiveData<String?>()
    val amountRangeError: LiveData<String?> = _amountRangeError

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _tradeTypeError = MutableLiveData<String?>()
    val tradeTypeError: LiveData<String?> = _tradeTypeError

    private val _paymentOptionsError = MutableLiveData<String?>()
    val paymentOptionsError: LiveData<String?> = _paymentOptionsError

    private val _termsError = MutableLiveData<String?>()
    val termsError: LiveData<String?> = _termsError

    private val _priceError = MutableLiveData<String?>()
    val priceError: LiveData<String?> = _priceError

    private val _amountMinLimit = MutableLiveData<Int>()

    private val _amountMaxLimit = MutableLiveData<Int>()

    val cryptoAmountFormatted: LiveData<String> =
        TripleCombinedLiveData(price, _amountMaxLimit, selectedCoin) { price, maxAmount, coin ->
            val cryptoAmount = if (maxAmount == null || price == null || price == 0.0) {
                0.0
            } else {
                maxAmount.toDouble() / price
            }
            stringProvider.getString(
                R.string.trade_crypto_amount_value,
                cryptoAmount.toStringCoin(),
                coin?.code.orEmpty()
            )
        }

    init {
        fetchInitialData()
    }

    fun fetchInitialData() {
        _initialLoadingData.value = LoadingData.Loading(Unit)
        getAvailableTradePaymentOptionsUseCase(Unit, onSuccess = { availablePaymentOptions ->
            _availablePaymentOptions.value = availablePaymentOptions
            getCoinListUseCase(
                params = Unit,
                onSuccess = { coinsDataList ->
                    coinList = coinsDataList
                    val coin = coinList.firstOrNull()
                    _initialLoadingData.value = coin?.let {
                        _selectedCoin.value = it
                        LoadingData.Success(Unit)
                    } ?: LoadingData.Error(Failure.ServerError())
                },
                onError = { _initialLoadingData.value = LoadingData.Error(Failure.ServerError()) }
            )
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
        })
    }

    fun changePaymentSelection(paymentOption: AvailableTradePaymentOption) {
        _availablePaymentOptions.value = availablePaymentOptions.value.orEmpty().map {
            if (it.id == paymentOption.id) {
                it.copy(selected = !paymentOption.selected)
            } else {
                it
            }
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
        _selectedCoin.value = coinDataItem
    }

    fun showLocationError() {
        _createTradeLoadingData.value = LoadingData.Error(
            Failure.LocationError(
                stringProvider.getString(R.string.location_required_on_trade_creation)
            )
        )
    }

    fun createTrade(@TradeType type: Int, terms: String) {
        val paymentOptions = availablePaymentOptions.value.orEmpty()
            .asSequence()
            .filter(AvailableTradePaymentOption::selected)
            .map { it.payment.paymentId }
            .toList()
        var errorCount = 0
        if (type == -1) {
            _tradeTypeError.value =
                stringProvider.getString(R.string.trade_type_not_selected_error_message)
            errorCount++
        } else {
            _tradeTypeError.value = null
        }
        if (paymentOptions.isEmpty()) {
            _paymentOptionsError.value =
                stringProvider.getString(R.string.create_trade_no_payment_options_selected_error)
            errorCount++
        } else {
            _paymentOptionsError.value = null
        }
        val price = price.value ?: 0.0
        if (price <= 0.0) {
            _priceError.value = stringProvider.getString(R.string.create_trade_price_zero_error)
            errorCount++
        } else {
            _priceError.value = null
        }
        val fromAmount = _amountMinLimit.value ?: 0
        val toAmount = _amountMaxLimit.value ?: 0
        when {
            fromAmount == 0 || toAmount == 0 -> {
                _amountRangeError.value =
                    stringProvider.getString(R.string.create_trade_amount_range_zero_error)
                errorCount++
            }
            toAmount < fromAmount -> {
                _amountRangeError.value =
                    stringProvider.getString(R.string.create_trade_amount_range_error)
                errorCount++
            }
            else -> _amountRangeError.value = null
        }
        if (terms.isEmpty()) {
            _termsError.value =
                stringProvider.getString(R.string.create_trade_terms_validation_error)
            errorCount++
        } else {
            _termsError.value = null
        }
        if (errorCount > 0) {
            return
        }
        val fee = serviceInfoProvider.getService(ServiceType.TRADE)?.feePercent ?: 0.0
        val cryptoAmount = toAmount / price * (1 + fee / 100)
        if (type == TradeType.SELL && cryptoAmount > selectedCoin.value?.reservedBalanceCoin ?: 0.0) {
            _amountRangeError.value =
                stringProvider.getString(R.string.create_trade_not_enough_crypto_balance)
            return
        }
        val service = serviceInfoProvider.getService(ServiceType.TRADE)
        if (service == null || service.txLimit < toAmount || service.remainLimit < toAmount) {
            _createTradeLoadingData.value = LoadingData.Error(
                Failure.MessageError(
                    stringProvider.getString(R.string.limits_exceeded_validation_message)
                )
            )
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
                    _createTradeLoadingData.value =
                        LoadingData.Error(Failure.ClientValidationError())
                }
            },
            onError = {
                _createTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }

    private fun createTrade(
        @TradeType type: Int,
        coinCode: String,
        price: Double,
        fromAmount: Int,
        toAmount: Int,
        terms: String,
        paymentOptions: List<@PaymentOption Int>
    ) {
        val serviceFee = serviceInfoProvider.getService(ServiceType.TRADE)?.feePercent ?: 0.0
        _createTradeLoadingData.value = LoadingData.Loading()
        createTradeUseCase(
            CreateTradeItem(
                type, coinCode, price.toInt(),
                fromAmount, toAmount, terms,
                serviceFee, toAmount.toDouble(), paymentOptions
            ), onSuccess = { createTradeResult ->
                if (type == TradeType.SELL) {
                    updateReservedBalanceUseCase(
                        params = UpdateReservedBalanceUseCase.Params(
                            coinCode = coinCode,
                            txCryptoAmount = toAmount / price * (1 + serviceFee / 100 / 2),
                            txAmount = toAmount * (1 + serviceFee / 100 / 2),
                            txFee = 0.0,
                            maxAmountUsed = false,
                        ),
                        onSuccess = {
                            _createTradeLoadingData.value = LoadingData.Success(it)
                        },
                        onError = {
                            _createTradeLoadingData.value = LoadingData.Error(it)
                        }
                    )
                } else {
                    _createTradeLoadingData.value = LoadingData.Success(createTradeResult)
                }
            }, onError = {
                _createTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }
}
