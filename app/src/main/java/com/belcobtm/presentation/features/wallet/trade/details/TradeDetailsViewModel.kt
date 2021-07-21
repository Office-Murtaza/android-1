package com.belcobtm.presentation.features.wallet.trade.details

import androidx.annotation.DrawableRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.data.inmemory.trade.TradeInMemoryCache.Companion.UNDEFINED_DISTANCE
import com.belcobtm.data.model.trade.TradeType
import com.belcobtm.domain.trade.details.GetTradeDetailsUseCase
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.presentation.core.formatter.Formatter
import com.belcobtm.presentation.core.formatter.GoogleMapsDirectionQueryFormatter
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.features.wallet.trade.list.model.TradePayment

class TradeDetailsViewModel(
    private val getTradeDetailsUseCase: GetTradeDetailsUseCase,
    private val stringProvider: StringProvider,
    private val priceFormatter: Formatter<Double>,
    private val googleMapQueryFormatter: Formatter<GoogleMapsDirectionQueryFormatter.Location>
) : ViewModel() {

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _selectedCoin = MutableLiveData<LocalCoinType>()
    val selectedCoin: LiveData<LocalCoinType> = _selectedCoin

    private val _tradeType = MutableLiveData<Pair<@TradeType Int, Boolean>>()
    val tradeType: LiveData<Pair<@TradeType Int, Boolean>> = _tradeType

    private val _price = MutableLiveData<String>()
    val price: LiveData<String> = _price

    private val _publicId = MutableLiveData<String>()
    val publicId: LiveData<String> = _publicId

    private val _traderStatus = MutableLiveData<@DrawableRes Int>()
    val traderStatus: LiveData<Int> = _traderStatus

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

    private var toTradeLat: Double? = null
    private var toTradeLong: Double? = null

    fun fetchTradeDetails(tradeId: String) {
        _initialLoadingData.value = LoadingData.Loading()
        getTradeDetailsUseCase.invoke(tradeId, onSuccess = {
            _selectedCoin.value = it.coin
            _price.value = priceFormatter.format(it.price)
            val isOutOfStock = it.minLimit > it.maxLimit
            _amountRange.value = if (isOutOfStock) {
                stringProvider.getString(R.string.trade_amount_range_out_of_stock)
            } else {
                stringProvider.getString(
                    R.string.trade_list_item_price_range_format,
                    it.minLimitFormatted,
                    it.maxLimitFormatted
                )
            }
            _paymentOptions.value = it.paymentMethods
            _traderRate.value = it.makerTradingRate
            _totalTrades.value = it.makerTotalTradesFormatted
            _publicId.value = it.makerPublicId
            _traderStatus.value = it.makerStatusIcon
            _tradeType.value = it.tradeType to !isOutOfStock
            _terms.value = it.terms
            if (it.distance != UNDEFINED_DISTANCE) {
                _distance.value = it.distanceFormatted
            }
            toTradeLat = it.makerLatitude
            toTradeLong = it.makerLongitude
            _initialLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(it)
        })
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