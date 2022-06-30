package com.belcobtm.presentation.screens.wallet.trade.order.rate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.R
import com.belcobtm.domain.trade.order.RateOrderUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.provider.string.StringProvider

class TradeOrderRateViewModel(
    private val rateOrderUseCase: RateOrderUseCase,
    private val stringProvider: StringProvider
) : ViewModel() {

    companion object {
        const val LOW_RATING = 1
        const val BAD_RATING = 2
        const val NORMAL_RATING = 3
        const val GOOD_RATING = 4
        const val PERFECT_RATING = 5
    }

    private val _rateLabel = MutableLiveData<String>()
    val rateLabel: LiveData<String> = _rateLabel

    private var rateValue: Int = GOOD_RATING

    private val _rateLoadingData = MutableLiveData<LoadingData<Unit>>()
    val rateLoadingData: LiveData<LoadingData<Unit>> = _rateLoadingData

    init {
        onRateChanged(rateValue)
    }

    fun onRateChanged(rating: Int) {
        val labelId = when (rating) {
            LOW_RATING -> R.string.low_rating_label
            BAD_RATING -> R.string.bad_rating_label
            NORMAL_RATING -> R.string.normal_rating_label
            GOOD_RATING -> R.string.good_rating_label
            PERFECT_RATING -> R.string.perfect_rating_label
            else -> throw IllegalArgumentException("IllegalRating value")
        }
        rateValue = rating
        _rateLabel.value = stringProvider.getString(labelId)
    }

    fun rateOrder(orderId: String) {
        _rateLoadingData.value = LoadingData.Loading()
        rateOrderUseCase(
            RateOrderUseCase.Params(orderId, rateValue),
            onSuccess = {
                _rateLoadingData.value = LoadingData.Success(Unit)
            }, onError = {
                _rateLoadingData.value = LoadingData.Error(it)
            })
    }
}