package com.app.belcobtm.presentation.features.wallet.trade.mytrade.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.trade.details.CancelTradeUseCase
import com.app.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class MyTradeDetailsViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val cancelTradeUseCase: CancelTradeUseCase,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _cancelTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val cancelTradeLoadingData: LiveData<LoadingData<Unit>> = _cancelTradeLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

    private val _cryptoAmountError = MutableLiveData<String?>()
    val cryptoAmountError: LiveData<String?> = _cryptoAmountError

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _ordersCount = MutableLiveData<Int>()
    val ordersCount: LiveData<Int> = _ordersCount

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _amountRange = MutableLiveData<String>()
    val amountRange: LiveData<String> = _amountRange

    fun fetchTradeDetails(tradeId: Int) {
        _initialLoadingData.value = LoadingData.Loading()
        getTradeDetailsUseCase.invoke(tradeId, onSuccess = {
            _selectedCoin.value = it.coin
            _price.value = priceFormatter.format(it.price)
            _amountRange.value = stringProvider.getString(
                R.string.trade_list_item_price_range_format,
                priceFormatter.format(it.minLimit),
                priceFormatter.format(it.maxLimit)
            )
            _paymentOptions.value = it.paymentMethods
            _ordersCount.value = it.ordersCount
            _terms.value = it.terms
            _initialLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(it)
        })
    }

    fun cancel(tradeId: Int) {
        _cancelTradeLoadingData.value = LoadingData.Loading()
        cancelTradeUseCase(tradeId, onSuccess = {
            _cancelTradeLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _cancelTradeLoadingData.value = LoadingData.Error(it)
        })
    }
}