package com.app.belcobtm.presentation.features.wallet.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.disk.database.AccountDao
import com.app.belcobtm.data.websockets.base.model.WalletBalance
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.balance.adapter.CoinListItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class WalletViewModel(
    private val accountDao: AccountDao,
    private val walletObserver: WalletObserver
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

    private suspend fun mapWalletBalance(
        wallet: WalletBalance
    ): LoadingData<Pair<Double, List<CoinListItem>>> =
        when (wallet) {
            is WalletBalance.NoInfo -> LoadingData.Loading()
            is WalletBalance.Error -> LoadingData.Error(wallet.error)
            is WalletBalance.Balance -> {
                wallet.data.coinList
                    .filter { accountDao.getItem(it.code).isEnabled }
                    .map {
                        CoinListItem(
                            code = it.code,
                            balanceCrypto = it.balanceCoin + it.reservedBalanceCoin,
                            balanceFiat = it.balanceUsd + it.reservedBalanceUsd,
                            priceUsd = it.priceUsd
                        )
                    }.let { coinList ->
                        LoadingData.Success(wallet.data.balance to coinList)
                    }
            }
        }
}
