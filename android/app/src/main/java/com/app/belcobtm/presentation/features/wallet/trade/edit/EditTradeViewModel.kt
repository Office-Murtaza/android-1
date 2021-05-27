package com.app.belcobtm.presentation.features.wallet.trade.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.trade.details.EditTradeUseCase
import com.app.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.presentation.core.extensions.toStringCoin
import com.app.belcobtm.presentation.core.livedata.TripleCombinedLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption

class EditTradeViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase,
    private val getCoinListUseCase: GetCoinListUseCase,
    private val editTradeUseCase: EditTradeUseCase,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _editTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val editTradeLoadingData: LiveData<LoadingData<Unit>> = _editTradeLoadingData

    private val _selectedCoin = MutableLiveData<CoinDataItem>()
    val selectedCoin: LiveData<CoinDataItem> = _selectedCoin

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?> = _cryptoAmountError

    private val _price = MutableLiveData<Double>(0.0)
    val price: LiveData<Double> = _price

    private val _initialPrice = MutableLiveData<Double>(0.0)
    val initialPrice: LiveData<Double> = _initialPrice

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
                _tradeType.value = trade.type
                _initialTerms.value = trade.terms
                _amountMinLimit.value = trade.minLimit.toInt()
                _amountMaxLimit.value = trade.maxLimit.toInt()
                _price.value = trade.price
                _initialPrice.value = trade.price
                getCoinListUseCase(
                    params = Unit,
                    onSuccess = { coinsDataList ->
                        val coin: CoinDataItem? = coinsDataList.firstOrNull { it.code == trade.coin.name }
                        if (coin == null) {
                            _initialLoadingData.value = LoadingData.Error(Failure.ServerError())
                        } else {
                            _selectedCoin.value = coin
                            _initialLoadingData.value = LoadingData.Success(Unit)
                        }
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

    fun editTrade(
        tradeId: String,
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
            _snackbarMessage.value = stringProvider.getString(R.string.edit_trade_no_payment_options_selected_error)
            return
        }
        val price = price.value ?: 0.0
        if (price <= 0.0) {
            _priceError.value = stringProvider.getString(R.string.edit_trade_price_zero_error)
            return
        } else {
            _priceError.value = null
        }
        val fromAmount = _amountMinLimit.value ?: 0
        val toAmount = _amountMaxLimit.value ?: 0
        if (
            fromAmount < minRangeAmount || fromAmount > maxRangeAmount
            || toAmount < minRangeAmount || toAmount > maxRangeAmount
        ) {
            _priceRangeError.value = stringProvider.getString(R.string.edit_trade_amount_range_error)
            return
        } else {
            _priceRangeError.value = null
        }
        val cryptoAmount = toAmount / price
        if (tradeType.value == TradeType.SELL && cryptoAmount > selectedCoin.value?.reservedBalanceCoin ?: 0.0) {
            _snackbarMessage.value = stringProvider.getString(R.string.edit_trade_not_enough_crypto_balance)
            return
        }
        _editTradeLoadingData.value = LoadingData.Loading()
        editTradeUseCase(
            EditTradeItem(tradeId, price, fromAmount, toAmount, terms, paymentOptions), onSuccess = {
                _editTradeLoadingData.value = LoadingData.Success(it)
            }, onError = {
                _editTradeLoadingData.value = LoadingData.Error(it)
            }
        )
    }
}