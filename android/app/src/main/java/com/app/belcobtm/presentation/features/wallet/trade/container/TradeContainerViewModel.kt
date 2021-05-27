package com.app.belcobtm.presentation.features.wallet.trade.container

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.base.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.trade.ClearCacheUseCase
import com.app.belcobtm.domain.trade.list.FetchTradesUseCase
import com.app.belcobtm.domain.trade.list.StartObserveTradeDataUseCase
import com.app.belcobtm.domain.trade.list.StopObserveTradeDataUseCase
import com.app.belcobtm.domain.wallet.interactor.ConnectToWalletUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TradeContainerViewModel(
    private val fetchTradesUseCase: FetchTradesUseCase,
    private val startObserveTradeDataUseCase: StartObserveTradeDataUseCase,
    private val stopObserveTradeDataUseCase: StopObserveTradeDataUseCase,
    private val clearCacheUseCase: ClearCacheUseCase,
    private val walletObserver: WalletObserver,
    private val connectToWalletUseCase: ConnectToWalletUseCase
) : ViewModel() {

    private val _loadingData = MutableLiveData<LoadingData<Unit>>()
    val loadingData: LiveData<LoadingData<Unit>>
        get() = _loadingData

    private var calculateDistanceEnabled = false

    fun checkBalanceAndFetchTrades(calculateDistanceEnabled: Boolean) {
        this.calculateDistanceEnabled = calculateDistanceEnabled
        _loadingData.value = LoadingData.Loading()
        viewModelScope.launch {
            walletObserver.observe()
                .receiveAsFlow()
                .collect {
                    when (it) {
                        is WalletBalance.Balance -> fetchTrades(calculateDistanceEnabled)
                        is WalletBalance.Error -> _loadingData.value = LoadingData.Error(Failure.WalletFetchError())
                        else -> _loadingData.value = LoadingData.Loading()
                    }
                }
        }
    }

    private fun fetchTrades(calculateDistanceEnabled: Boolean) {
        _loadingData.value = LoadingData.Loading()
        fetchTradesUseCase.invoke(FetchTradesUseCase.Params(calculateDistanceEnabled),
            onSuccess = {
                _loadingData.value = LoadingData.Success(Unit)
            }, onError = {
                _loadingData.value = LoadingData.Error(it)
            })
    }

    fun showError(error: Failure) {
        _loadingData.value = LoadingData.Error(error)
    }

    fun retry() {
        val value = _loadingData.value
        if (value !is LoadingData.Error<Unit>) {
            return
        }
        if (value.errorType is Failure.WalletFetchError) {
            connectToWalletUseCase(Unit)
        } else {
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