package com.app.belcobtm.presentation.features.wallet.trade.list.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.R
import com.app.belcobtm.data.model.trade.filter.SortOption
import com.app.belcobtm.domain.trade.list.filter.ApplyFilterUseCase
import com.app.belcobtm.domain.trade.list.filter.LoadFilterDataUseCase
import com.app.belcobtm.domain.trade.list.filter.ResetFilterUseCase
import com.app.belcobtm.presentation.core.parser.StringParser
import com.app.belcobtm.presentation.core.provider.string.StringProvider
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.TradeFilterItem

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
    val distanceMinLimit: LiveData<Int>
        get() = _distanceMinLimit

    private val _distanceMaxLimit = MutableLiveData<Int>()
    val distanceMaxLimit: LiveData<Int>
        get() = _distanceMaxLimit

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
            _paymentOptions.value = it.paymentOptions
            _coins.value = it.coins
            _distanceEnabled.value = it.distanceFilterEnabled
            _distanceMinLimit.value = it.minDistance
            _distanceMaxLimit.value = it.maxDistance
            _sortOption.value = it.sortOption
        }, onError = {

        })
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

    fun resetFilter() {
        resetFilterUseCase.invoke(Unit, onSuccess = {
            fetchInitialData()
        })
    }

    fun applyFilter(minAvailableDistance: Int, maxAvailableDistance: Int) {
        val distanceEnabled = distanceEnabled.value ?: false
        val minDistance = distanceMinLimit.value ?: 0
        val maxDistance = distanceMaxLimit.value ?: 0
        if (
            distanceEnabled && (
                    minDistance < minAvailableDistance || minDistance > maxAvailableDistance
                            || maxDistance < minAvailableDistance || maxDistance > maxAvailableDistance ||
                            minDistance > maxDistance
                    )
        ) {
            _distanceRangeError.value = stringProvider.getString(
                R.string.create_trade_amount_range_error,
                stringProvider.getString(R.string.distance_label_formatted, minAvailableDistance),
                stringProvider.getString(R.string.distance_label_formatted, maxAvailableDistance)
            )
            return
        } else {
            _distanceRangeError.value = null
        }
        val newFilter = TradeFilterItem(
            coins.value.orEmpty(), paymentOptions.value.orEmpty(),
            distanceEnabled, minDistance, maxDistance,
            sortOption.value ?: SortOption.PRICE
        )
        applyFilterUseCase.invoke(newFilter, onSuccess = {
            _closeFilter.value = true
        })
    }

    fun parseDistance(input: String): Int = distanceParser.parse(input)

    fun formatDistance(distance: Int): String = stringProvider.getString(R.string.distance_label_formatted, distance)

    fun selectSort(@SortOption option: Int) {
        _sortOption.value = option
    }

}