package com.app.belcobtm.presentation.features.wallet.trade.container

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.ClearCacheUseCase
import com.app.belcobtm.domain.trade.list.FetchTradesUseCase
import com.app.belcobtm.domain.trade.list.StartObserveTradeDataUseCase
import com.app.belcobtm.domain.trade.list.StopObserveTradeDataUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class TradeContainerViewModel(
    private val fetchTradesUseCase: FetchTradesUseCase,
    private val startObserveTradeDataUseCase: StartObserveTradeDataUseCase,
    private val stopObserveTradeDataUseCase: StopObserveTradeDataUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    private val _loadingData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>>
        get() = _loadingData

    private var calculateDistanceEnabled = false

    fun fetchTrades(calculateDistanceEnabled: Boolean) {
        this.calculateDistanceEnabled = calculateDistanceEnabled
        _loadingData.value = LoadingData.Loading()
        fetchTradesUseCase.invoke(FetchTradesUseCase.Params(calculateDistanceEnabled),
            onSuccess = {
                viewModelScope.launch {
                    _loadingData.value = LoadingData.Success(Unit)
                }
            }, onError = {
                _loadingData.value = LoadingData.Error(it)
            })
    }

    fun showError(error: Failure) {
        _loadingData.value = LoadingData.Error(error)
    }

    fun retry() {
        if (_loadingData.value is LoadingData.Error<Unit>) {
            fetchTrades(calculateDistanceEnabled)
        }
    }

    fun subscribeOnUpdates() {
        startObserveTradeDataUseCase(Unit)
    }

    fun unsubscribeFromUpdates() {
        stopObserveTradeDataUseCase(Unit)
        clearCacheUseCase(Unit)
    }
}