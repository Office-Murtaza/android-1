package com.app.belcobtm.presentation.features.wallet.trade.container

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.list.FetchTradesUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.launch

class TradeContainerViewModel(
    private val fetchTradesUseCase: FetchTradesUseCase
) : ViewModel() {

    private val _loadingData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>>
        get() = _loadingData

    fun fetchTrades() {
        _loadingData.value = LoadingData.Loading()
        fetchTradesUseCase.invoke(Unit,
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
            fetchTrades()
        }
    }
}