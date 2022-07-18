package com.belcobtm.presentation.screens.wallet.trade.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.R
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.details.ObserveTradeDetailsUseCase
import com.belcobtm.domain.trade.model.trade.TradeType
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradeItem
import com.belcobtm.presentation.screens.wallet.trade.list.model.TradePayment
import com.belcobtm.presentation.tools.formatter.Formatter
import com.belcobtm.presentation.tools.formatter.GoogleMapsDirectionQueryFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class TradeDetailsViewModel(
    private val observeTradeDetailsUseCase: ObserveTradeDetailsUseCase,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>,
    private val googleMapQueryFormatter: Formatter<GoogleMapsDirectionQueryFormatter.Location>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

    private val _tradeType = MutableLiveData<Pair<TradeType, Boolean>>()
    val tradeType: LiveData<Pair<TradeType, Boolean>> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _publicId = MutableLiveData<String>()
    val publicId: LiveData<String> = _publicId

    private val _traderRate = MutableLiveData<Double>()
    val traderRate: LiveData<Double> = _traderRate

    private val _totalTrades = MutableLiveData<String>()
    val totalTrades: LiveData<String> = _totalTrades

    private val _distance = MutableLiveData<String>()
    val distance: LiveData<String> = _distance

    private val _terms = MutableLiveData<String>()
    val terms: LiveData<String> = _terms

    private val _paymentOptions = MutableLiveData<List<TradePayment>>()
    val paymentOptions: LiveData<List<TradePayment>> = _paymentOptions

    private val _amountRange = MutableLiveData<String>()
    val amountRange: LiveData<String> = _amountRange

    private val _isOutOfStock = MutableLiveData<Boolean>()
    val isOutOfStock: LiveData<Boolean> = _isOutOfStock

    private var toTradeLat: Double? = null
    private var toTradeLong: Double? = null

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading()
        viewModelScope.launch {
            observeTradeDetailsUseCase(tradeId)
                .filterNotNull()
                .collectLatest {
                    if (it.isRight) {
                        updateTradeData(
                            tradeItem = (it as Either.Right<TradeItem>).b
                        )
                    } else {
                        _initialLoadingData.value = LoadingData.Error(
                            (it as Either.Left<Failure>).a
                        )
                    }
                }
        }
    }

    private fun updateTradeData(tradeItem: TradeItem) {
        _selectedCoin.value = tradeItem.coin
        _price.value = priceFormatter.format(tradeItem.price)
        val isOutOfStock = tradeItem.minLimit > tradeItem.maxLimit
        _isOutOfStock.value = isOutOfStock
        _amountRange.value = if (isOutOfStock) {
            stringProvider.getString(R.string.trade_amount_range_out_of_stock)
        } else {
            stringProvider.getString(
                R.string.trade_list_item_price_range_format,
                tradeItem.minLimitFormatted,
                tradeItem.maxLimitFormatted
            )
        }
        _paymentOptions.value = tradeItem.paymentMethods
        _traderRate.value = tradeItem.makerTradingRate
        _totalTrades.value = tradeItem.makerTotalTradesFormatted
        _publicId.value = tradeItem.makerPublicId
        _tradeType.value = tradeItem.tradeType to !isOutOfStock
        _terms.value = tradeItem.terms
        _distance.value = tradeItem.distanceFormatted
        toTradeLat = tradeItem.makerLatitude
        toTradeLong = tradeItem.makerLongitude
        _initialLoadingData.value = LoadingData.Success(Unit)
    }

    fun getQueryForMap(): String? {
        val toLat = toTradeLat ?: return null
        val toLong = toTradeLong ?: return null
        return googleMapQueryFormatter.format(
            GoogleMapsDirectionQueryFormatter.Location(
                toLat,
                toLong
            )
        )
    }

}
