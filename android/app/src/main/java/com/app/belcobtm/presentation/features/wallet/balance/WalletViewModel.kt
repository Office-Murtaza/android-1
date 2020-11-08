package com.app.belcobtm.presentation.features.wallet.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.balance.adapter.BalanceListItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WalletViewModel(private val walletObserver: WalletObserver) : ViewModel() {

    val balanceLiveData: LiveData<LoadingData<Pair<Double, List<BalanceListItem.Coin>>>> =
        walletObserver.observe()
            .map { mapWalletBalance(it) }
            .asLiveData(viewModelScope.coroutineContext)

    fun reconnectToWallet() {
        viewModelScope.launch {
            walletObserver.connect()
        }
    }

    private fun mapWalletBalance(
        wallet: WalletBalance
    ): LoadingData<Pair<Double, List<BalanceListItem.Coin>>> =
        when (wallet) {
            is WalletBalance.NoInfo -> LoadingData.Loading()
            is WalletBalance.Error -> LoadingData.Error(wallet.error)
            is WalletBalance.Balance ->
                wallet.data.coinList.map {
                    BalanceListItem.Coin(
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