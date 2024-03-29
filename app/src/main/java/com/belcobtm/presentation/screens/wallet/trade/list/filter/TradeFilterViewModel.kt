package com.belcobtm.presentation.screens.wallet.trade.list.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.domain.trade.list.filter.ApplyFilterUseCase
import com.belcobtm.domain.trade.list.filter.LoadFilterDataUseCase
import com.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.belcobtm.domain.trade.model.filter.SortOption
import com.belcobtm.presentation.core.parser.StringParser
import com.belcobtm.presentation.core.provider.string.StringProvider
import com.belcobtm.presentation.screens.wallet.trade.create.model.AvailableTradePaymentOption
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.CoinCodeListItem
import com.belcobtm.presentation.screens.wallet.trade.list.filter.model.TradeFilterItem

class TradeFilterViewModel(
    private val loadFilterDataUseCase: LoadFilterDataUseCase,
    private val resetFilterUseCase: ResetFilterUseCase,
    private val applyFilterUseCase: ApplyFilterUseCase,
    private val stringProvider: StringProvider,
    private val distanceParser: StringParser<Int>
) : ViewModel() {

    private val _coins = MutableLiveData<List<CoinCodeListItem>>()
    val coins: LiveData<List<CoinCodeListItem>>
        get() = _coins

    private val _paymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val paymentOptions: LiveData<List<AvailableTradePaymentOption>>
        get() = _paymentOptions

    private val _distanceMinLimit = MutableLiveData<Int>()

    private val _distanceMaxLimit = MutableLiveData<Int>()

    private val _initialDistanceMinLimit = MutableLiveData<Int>()
    val initialDistanceMinLimit: LiveData<Int> = _initialDistanceMinLimit

    private val _initialDistanceMaxLimit = MutableLiveData<Int>()
    val initialDistanceMaxLimit: LiveData<Int> = _initialDistanceMaxLimit

    private val _distanceRangeError = MutableLiveData<String?>()
    val distanceRangeError: LiveData<String?>
        get() = _distanceRangeError

    private val _distanceEnabled = MutableLiveData<Boolean>()
    val distanceEnabled: LiveData<Boolean>
        get() = _distanceEnabled

    private val _sortOption = MutableLiveData<@SortOption Int>()
    val sortOption: LiveData<@SortOption Int>
        get() = _sortOption

    private val _closeFilter = MutableLiveData<Boolean>()
    val closeFilter: LiveData<Boolean>
        get() = _closeFilter

    fun fetchInitialData() {
        loadFilterDataUseCase.invoke(Unit, onSuccess = {
            processFilterData(it)
        })
    }

    private fun processFilterData(data: TradeFilterItem) {
        _paymentOptions.value = data.paymentOptions
        _coins.value = data.coins
        _distanceEnabled.value = data.distanceFilterEnabled
        _distanceMinLimit.value = data.minDistance
        _initialDistanceMinLimit.value = data.minDistance
        _distanceMaxLimit.value = data.maxDistance
        _initialDistanceMaxLimit.value = data.maxDistance
        _sortOption.value = data.sortOption
    }

    fun updateMinDistance(distance: Int) {
        _distanceMinLimit.value = distance
    }

    fun updateMaxDistance(distance: Int) {
        _distanceMaxLimit.value = distance
    }

    fun selectCoin(coinListItem: CoinCodeListItem) {
        _coins.value = coins.value.orEmpty().map {
            it.copy(selected = it.id == coinListItem.id)
        }
    }

    fun changePaymentSelection(paymentOption: AvailableTradePaymentOption) {
        _paymentOptions.value = paymentOptions.value.orEmpty().map {
            if (it.id == paymentOption.id) {
                it.copy(selected = !paymentOption.selected)
            } else {
                it
            }
        }
    }

    fun resetFilter() {
        resetFilterUseCase.invoke(Unit, onSuccess = {
            loadFilterDataUseCase.invoke(Unit, onSuccess = {
                processFilterData(it)
                applyFilter()
            })
        })
    }

    fun applyFilter() {
        val distanceEnabled = distanceEnabled.value ?: false
        val minDistance = _distanceMinLimit.value ?: 0
        val maxDistance = _distanceMaxLimit.value ?: 0
        when {
            distanceEnabled && minDistance > maxDistance -> {
                _distanceRangeError.value = stringProvider.getString(R.string.create_trade_amount_range_error)
                return
            }
            distanceEnabled && maxDistance == 0 -> {
                _distanceRangeError.value = stringProvider.getString(R.string.trade_filter_distance_range_zero_error)
                return
            }
            else -> {
                _distanceRangeError.value = null
            }
        }
        val newFilter = TradeFilterItem(
            coins = _coins.value.orEmpty(),
            paymentOptions = _paymentOptions.value.orEmpty(),
            distanceFilterEnabled = distanceEnabled,
            minDistance = minDistance,
            maxDistance = maxDistance,
            sortOption = _sortOption.value ?: SortOption.PRICE
        )
        applyFilterUseCase.invoke(newFilter, onSuccess = {
            _closeFilter.value = true
        })
    }

    fun parseDistance(input: String): Int = distanceParser.parse(input)

    fun selectSort(@SortOption option: Int) {
        _sortOption.value = option
    }

}