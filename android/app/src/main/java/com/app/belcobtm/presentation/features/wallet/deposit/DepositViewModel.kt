package com.app.belcobtm.presentation.features.wallet.deposit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DepositViewModel(
    private val coinCode: String,
    private val walletObserver: WalletObserver
) : ViewModel() {
    val addressLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        viewModelScope.launch {
            walletObserver.observe()
                .receiveAsFlow()
                .filterIsInstance<WalletBalance.Balance>()
                .collect { balance ->
                    addressLiveData.value = balance.data.coinList
                        .firstOrNull { it.code == coinCode }
                        ?.publicKey
                }
        }
    }
}