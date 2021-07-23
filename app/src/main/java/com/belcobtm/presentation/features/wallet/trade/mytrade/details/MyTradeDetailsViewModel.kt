package com.belcobtm.presentation.features.wallet.trade.mytrade.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.details.CancelTradeUseCase
import com.belcobtm.domain.trade.details.ObserveTradeDetailsUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.features.wallet.trade.list.model.TradePayment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class MyTradeDetailsViewModel(
    private val observeTradeDetailsUseCase: ObserveTradeDetailsUseCase,
    private val cancelTradeUseCase: CancelTradeUseCase,
    private val stringProvider: StringProvider
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _cancelTradeLoadingData = MutableLiveData<LoadingData<Unit>>()
    val cancelTradeLoadingData: LiveData<LoadingData<Unit>> = _cancelTradeLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

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

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading()
        viewModelScope.launch {
            observeTradeDetailsUseCase(tradeId)
                .filterNotNull()
                .collectLatest {
                    if (it.isRight) {
                        updateTradeData((it as Either.Right<TradeItem>).b)
                    } else {
                        _initialLoadingData.value = LoadingData.Error(
                            (it as Either.Left<Failure>).a
                        )
                    }
                }
        }
    }

    private fun updateTradeData(trade: TradeItem) {
        _selectedCoin.value = trade.coin
        _price.value = trade.priceFormatted
        _tradeType.value = trade.tradeType
        _amountRange.value = if (trade.minLimit > trade.maxLimit) {
            stringProvider.getString(R.string.trade_amount_range_out_of_stock)
        } else {
            stringProvider.getString(
                R.string.trade_list_item_price_range_format,
                trade.minLimitFormatted,
                trade.maxLimitFormatted
            )
        }
        _paymentOptions.value = trade.paymentMethods
        _ordersCount.value = trade.ordersCount
        _terms.value = trade.terms
        _initialLoadingData.value = LoadingData.Success(Unit)
    }

    fun cancel(tradeId: String) {
        _cancelTradeLoadingData.value = LoadingData.Loading()
        cancelTradeUseCase(tradeId, onSuccess = {
            _cancelTradeLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _cancelTradeLoadingData.value = LoadingData.Error(it)
        })
    }
}