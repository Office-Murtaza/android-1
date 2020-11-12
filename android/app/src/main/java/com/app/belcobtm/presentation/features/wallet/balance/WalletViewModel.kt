package com.app.belcobtm.presentation.features.wallet.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinListItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WalletViewModel(
    private val walletObserver: WalletObserver,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    val balanceLiveData: LiveData<LoadingData<Pair<Double, List<CoinListItem>>>> =
        walletObserver.observe()
            .receiveAsFlow()
            .map { mapWalletBalance(it) }
            .asLiveData()

    fun reconnectToWallet() {
        viewModelScope.launch {
            walletObserver.connect()
        }
    }

    private fun mapWalletBalance(
        wallet: WalletBalance
    ): LoadingData<Pair<Double, List<CoinListItem>>> =
        when (wallet) {
            is WalletBalance.NoInfo -> LoadingData.Loading()
            is WalletBalance.Error -> LoadingData.Error(wallet.error)
            is WalletBalance.Balance -> {
                // workaround to update wallet repository cache
                // this cache is used to initialize ViewModels
                // see AppModule.kt
                walletRepository.updateCoinsCache(wallet.data.coinList)

                wallet.data.coinList.map {
                    CoinListItem(
                        code = it.code,
                        balanceCrypto = it.balanceCoin,
                        balanceFiat = it.balanceUsd,
                        priceUsd = it.priceUsd
                    )
                }.let { coinList ->
                    LoadingData.Success(wallet.data.balance to coinList)
                }
            }

        }
}