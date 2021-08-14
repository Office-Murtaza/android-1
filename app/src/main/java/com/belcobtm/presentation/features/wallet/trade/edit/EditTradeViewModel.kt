package com.belcobtm.presentation.features.wallet.trade.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Failure
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.belcobtm.domain.trade.details.EditTradeUseCase
import com.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.belcobtm.domain.wallet.item.CoinDataItem
import com.belcobtm.presentation.core.extensions.toStringCoin
import com.belcobtm.presentation.core.livedata.TripleCombinedLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

class EditTradeViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val editTradeUseCase: EditTradeUseCase,
    private val stringProvider: StringProvider,
    private val serviceInfoProvider: ServiceInfoProvider
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _editTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val editTradeLoadingData: LiveData<LoadingData<Unit>> = _editTradeLoadingData

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?> = _cryptoAmountError

    private val _price = MutableLiveData(0.0)
    val price: LiveData<Double> = _price

    private val _initialPrice = MutableLiveData(0.0)
    val initialPrice: LiveData<Double> = _initialPrice

    private val _availablePaymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val availablePaymentOptions: LiveData<List<AvailableTradePaymentOption>> =
        _availablePaymentOptions

    private val _snackbarMessage = MutableLiveData<String>()
    val snackbarMessage: LiveData<String> = _snackbarMessage

    private val _priceRangeError = MutableLiveData<String?>()
    val priceRangeError: LiveData<String?> = _priceRangeError

    private val _priceError = MutableLiveData<String?>()
    val priceError: LiveData<String?> = _priceError

    private val _amountMinLimit = MutableLiveData<Int>()

    private val _amountMaxLimit = MutableLiveData<Int>()

    private val _initialAmountMinLimit = MutableLiveData<Int>()
    val initialAmountMinLimit: LiveData<Int> = _initialAmountMinLimit

    private val _initialAmountMaxLimit = MutableLiveData<Int>()
    val initialAmountMaxLimit: LiveData<Int> = _initialAmountMaxLimit

    private val _paymentOptionsError = MutableLiveData<String?>()
    val paymentOptionsError: LiveData<String?> = _paymentOptionsError

    private val _termsError = MutableLiveData<String?>()
    val termsError: LiveData<String?> = _termsError

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _initialTerms = MutableLiveData<String>()
    val initialTerms: LiveData<String> = _initialTerms

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

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading(Unit)
        getAvailableTradePaymentOptionsUseCase.invoke(Unit, onSuccess = { availablePaymentOptions ->
            getTradeDetailsUseCase.invoke(tradeId, onSuccess = { trade ->
                _availablePaymentOptions.value = availablePaymentOptions.map { paymentOption ->
                    paymentOption.copy(selected = trade.paymentMethods.contains(paymentOption.payment))
                }
                _tradeType.value = trade.tradeType
                _initialTerms.value = trade.terms
                _amountMinLimit.value = trade.minLimit.toInt()
                _amountMaxLimit.value = trade.maxLimit.toInt()
                _initialAmountMinLimit.value = trade.minLimit.toInt()
                _initialAmountMaxLimit.value = trade.maxLimit.toInt()
                _price.value = trade.price
                _initialPrice.value = trade.price
                getCoinListUseCase(
                    params = Unit,
                    onSuccess = { coinsDataList ->
                        val coin: CoinDataItem? = coinsDataList.firstOrNull {
                            it.code == trade.coin.name
                        }
                        _initialLoadingData.value = coin?.let {
                            _selectedCoin.value = it
                            LoadingData.Success(Unit)
                        } ?: LoadingData.Error(Failure.ServerError())
                    },
                    onError = {
                        _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                    }
                )
            }, onError = {
                _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
            })
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

    fun updatePrice(amount: Double) {
        _price.value = amount
    }

    fun updateMinAmount(amount: Int) {
        _amountMinLimit.value = amount
    }

    fun updateMaxAmount(amount: Int) {
        _amountMaxLimit.value = amount
    }

    fun editTrade(tradeId: String, terms: String) {
        val paymentOptions = availablePaymentOptions.value.orEmpty()
            .asSequence()
            .filter(AvailableTradePaymentOption::selected)
            .map { it.payment.paymentId }
            .toList()
        var errorCount = 0
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
                _priceRangeError.value =
                    stringProvider.getString(R.string.create_trade_amount_range_zero_error)
                errorCount++
            }
            toAmount < fromAmount -> {
                _priceRangeError.value =
                    stringProvider.getString(R.string.create_trade_amount_range_error)
                errorCount++
            }
            else -> _priceRangeError.value = null
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
        val fee = serviceInfoProvider.getServiceFee(ServiceType.TRADE)
        val cryptoAmount = toAmount / price * (1 + fee / 100)
        if (tradeType.value == TradeType.SELL && cryptoAmount > selectedCoin.value?.reservedBalanceCoin ?: 0.0) {
            _priceRangeError.value =
                stringProvider.getString(R.string.edit_trade_not_enough_crypto_balance)
            return
        }
        _editTradeLoadingData.value = LoadingData.Loading()
        editTradeUseCase(
            EditTradeItem(tradeId, price, fromAmount, toAmount, terms, paymentOptions),
            onSuccess = {
                _editTradeLoadingData.value = LoadingData.Success(it)
            },
            onError = {
                _editTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }
}