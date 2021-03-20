package com.app.belcobtm.presentation.features.wallet.trade.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.inmemory.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.app.belcobtm.data.model.trade.TradeType
import com.app.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.app.belcobtm.domain.wallet.LocalCoinType
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class TradeDetailsViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

    private val _tradeType = MutableLiveData<@TradeType Int>()
    val tradeType: LiveData<@TradeType Int> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _publicId = MutableLiveData<String>()
    val publicId: LiveData<String> = _publicId

    private val _traderStatus = MutableLiveData<@DrawableRes Int>()
    val traderStatus: LiveData<Int> = _traderStatus

    private val _traderRate = MutableLiveData<Double>()
    val traderRate: LiveData<Double> = _traderRate

    private val _totalTrades = MutableLiveData<Int>()
    val totalTrades: LiveData<Int> = _totalTrades

    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> = _distance

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
            _traderRate.value = it.makerTradingRate
            _totalTrades.value = it.makerTotalTrades
            _publicId.value = it.makerPublicId
            _traderStatus.value = it.makerStatusIcon
            _terms.value = it.terms
            if (it.distance != UNDEFINED_DISTANCE) {
                _distance.value = it.distanceFormatted
            }
            _initialLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(it)
        })
    }

}