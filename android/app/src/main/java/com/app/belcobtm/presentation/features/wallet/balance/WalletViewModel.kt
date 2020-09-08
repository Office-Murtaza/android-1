package com.app.belcobtm.presentation.features.wallet.balance

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.GetBalanceUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.balance.adapter.BalanceListItem

class WalletViewModel(private val balanceUseCase: GetBalanceUseCase) : ViewModel() {
    val balanceLiveData: MutableLiveData<LoadingData<Pair<Double, List<BalanceListItem.Coin>>>> = MutableLiveData()

    init {
        updateBalanceData()
    }

    fun updateBalanceData() {
        balanceLiveData.value = LoadingData.Loading()
        balanceUseCase.invoke(
            Unit,
            onSuccess = { dataItem ->
                val coinList = dataItem.coinList.map {
                    BalanceListItem.Coin(
                        code = it.code,
                        balanceCrypto = it.balanceCoin,
                        balanceFiat = it.balanceUsd,
                        priceUsd = it.priceUsd
                    )
                }
                balanceLiveData.value = LoadingData.Success(Pair(dataItem.balance, coinList))
            },
            onError = { balanceLiveData.value = LoadingData.Error(it) }
        )
    }
}