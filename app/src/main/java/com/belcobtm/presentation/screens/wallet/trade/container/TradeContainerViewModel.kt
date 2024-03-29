package com.belcobtm.presentation.screens.wallet.trade.container

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.Failure
import com.belcobtm.domain.trade.ClearCacheUseCase
import com.belcobtm.domain.trade.list.FetchTradesUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class TradeContainerViewModel(
    private val fetchTradesUseCase: FetchTradesUseCase,
    private val clearCacheUseCase: ClearCacheUseCase
) : ViewModel() {

    var isArgsProcessed: Boolean = false

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

    fun unsubscribeFromUpdates() {
        clearCacheUseCase(Unit)
    }

}
