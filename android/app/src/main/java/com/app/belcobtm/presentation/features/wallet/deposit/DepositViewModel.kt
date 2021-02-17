package com.app.belcobtm.presentation.features.wallet.deposit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.belcobtm.data.websockets.wallet.WalletObserver
import com.app.belcobtm.data.websockets.wallet.model.WalletBalance
import com.app.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DepositViewModel(
    coinCode: String,
    getCoinByCodeUseCase: GetCoinByCodeUseCase
) : ViewModel() {
    val addressLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        getCoinByCodeUseCase(
            coinCode,
            onSuccess = { addressLiveData.value = it.publicKey }
        )
    }
}
