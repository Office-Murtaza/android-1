package com.belcobtm.presentation.features.wallet.balance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.data.disk.database.wallet.FullCoinEntity
import com.belcobtm.data.disk.database.wallet.WalletDao
import com.belcobtm.data.disk.database.wallet.WalletEntity
import com.belcobtm.data.websockets.wallet.WalletConnectionHandler
import com.belcobtm.domain.settings.interactor.GetNeedToShowRestrictions
import com.belcobtm.domain.settings.interactor.SetNeedToShowRestrictionsUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.features.deals.staking.StakingTransactionState
import com.belcobtm.presentation.features.wallet.balance.adapter.CoinListItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class WalletViewModel(
    private val walletDao: WalletDao,
    private val walletConnectionHandler: WalletConnectionHandler,
    private val getNeedToShowRestrictions: GetNeedToShowRestrictions,
    private val setNeedToShowRestrictionsUseCase: SetNeedToShowRestrictionsUseCase
) : ViewModel() {

    private val _balanceLiveData = MutableLiveData<LoadingData<Pair<Double, List<CoinListItem>>>>()
    val balanceLiveData: LiveData<LoadingData<Pair<Double, List<CoinListItem>>>> = _balanceLiveData

    private val _needToShowRestrictions: SingleLiveData<Boolean> = SingleLiveData()
    val needToShowRestrictions: LiveData<Boolean> = _needToShowRestrictions

    fun reconnectToWallet() {
        viewModelScope.launch {
            walletConnectionHandler.connect()
        }
    }

    init {
        viewModelScope.launch {
            walletDao.observeCoins()
                .combine(walletDao.observeWallet()) { coins, wallet -> mapWalletBalance(coins, wallet) }
                .combine(walletConnectionHandler.observeConnectionFailure()) { wallet, error ->
                    error?.let { LoadingData.Error<Pair<Double, List<CoinListItem>>>(it) } ?: wallet
                }
                .collect {
                    _balanceLiveData.value = it
                    getNeedToShowRestrictions.invoke(Unit, onSuccess = { result ->
                        _needToShowRestrictions.postValue(result)
                    })
                }
        }
    }

    fun restrictionsShown() {
        setNeedToShowRestrictionsUseCase.invoke(SetNeedToShowRestrictionsUseCase.Params(false))
    }

    private fun mapWalletBalance(
        coins: List<FullCoinEntity>, walletEntity: WalletEntity?
    ): LoadingData<Pair<Double, List<CoinListItem>>> =
        if (coins.isEmpty()) {
            LoadingData.Loading()
        } else {
            val coinItems = coins.filter { it.accountEntity.isEnabled }
                .map {
                    CoinListItem(
                        code = it.coin.code,
                        balanceCrypto = it.coin.balance + it.coin.reservedBalance,
                        balanceFiat = it.coin.balanceUsd + it.coin.reservedBalanceUsd,
                        priceUsd = it.coin.price
                    )
                }
            LoadingData.Success((walletEntity?.totalBalance ?: 0.0) to coinItems)
        }
}
