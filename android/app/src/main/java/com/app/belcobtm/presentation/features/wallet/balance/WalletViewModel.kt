package com.app.belcobtm.presentation.features.wallet.balance

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.wallet.WalletSocketRepository
import com.app.belcobtm.domain.wallet.interactor.GetBalanceUseCase
import com.app.belcobtm.domain.wallet.item.BalanceDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.app.belcobtm.presentation.features.wallet.balance.adapter.BalanceListItem
import kotlinx.coroutines.*

@ExperimentalCoroutinesApi
@SuppressLint("LogNotTimber")
class WalletViewModel(
    private val balanceUseCase: GetBalanceUseCase,
    private val walletSocketRepository: WalletSocketRepository
) : ViewModel() {
    val balanceLiveData: MutableLiveData<LoadingData<Pair<Double, List<BalanceListItem.Coin>>>> =
        MutableLiveData()

    init {
        balanceLiveData.value = LoadingData.Loading()
    }

    fun subscribeToChannel() {
        walletSocketRepository.open()
        GlobalScope.launch {
            for (it in walletSocketRepository.getBalanceFlow()) {
                if (it.isLeft) {
                    CoroutineScope(Dispatchers.Main).launch {
                        updateOnError((it as Either.Left).a)
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        updateCoinsItems((it as Either.Right).b)
                    }

                }
            }
        }
    }

    fun closeChannel() {
        walletSocketRepository.close()
    }

    private fun updateOnError(it: Failure) {
        balanceLiveData.value = LoadingData.Error(it)
    }

    private fun updateCoinsItems(dataItem: BalanceDataItem) {
        val coinList = dataItem.coinList.map {
            BalanceListItem.Coin(
                code = it.code,
                balanceCrypto = it.balanceCoin,
                balanceFiat = it.balanceUsd,
                priceUsd = it.priceUsd
            )
        }
        balanceLiveData.value = LoadingData.Success(Pair(dataItem.balance, coinList))
    }
}