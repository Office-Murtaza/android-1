package com.app.belcobtm.presentation.features.wallet.trade.list.filter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.trade.create.GetAvailableTradePaymentOptionsUseCase
import com.app.belcobtm.domain.trade.list.filter.GetCoinsUseCase
import com.app.belcobtm.presentation.features.wallet.trade.create.model.AvailableTradePaymentOption
import com.app.belcobtm.presentation.features.wallet.trade.list.filter.model.CoinCodeListItem

class TradeFilterViewModel(
    private val getCoinsUseCase: GetCoinsUseCase,
    private val getAvailableTradePaymentOptionsUseCase: GetAvailableTradePaymentOptionsUseCase
) : ViewModel() {

    private val _coins = MutableLiveData<List<CoinCodeListItem>>()
    val coins: LiveData<List<CoinCodeListItem>>
        get() = _coins

    private val _paymentOptions = MutableLiveData<List<AvailableTradePaymentOption>>()
    val paymentOptions: LiveData<List<AvailableTradePaymentOption>>
        get() = _paymentOptions

    fun fetchInitialData() {
        getAvailableTradePaymentOptionsUseCase.invoke(Unit, onSuccess = {
            _paymentOptions.value = it
        })
        getCoinsUseCase.invoke(Unit, onSuccess = {
            _coins.value = it
        })
    }

    fun selectCoin(coinListItem: CoinCodeListItem, isChecked: Boolean) {
        val currentList = coins.value
    }

}