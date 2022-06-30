package com.belcobtm.presentation.screens.wallet.deposit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.wallet.interactor.GetCoinByCodeUseCase

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
