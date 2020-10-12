package com.app.belcobtm.presentation.features.wallet.deposit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.wallet.interactor.GetCoinListUseCase

class DepositViewModel(
    private val coinCode: String,
    private val getCoinListUseCase: GetCoinListUseCase
) : ViewModel() {
    val addressLiveData: MutableLiveData<String> = MutableLiveData()

    init {
        getCoinListUseCase.invoke().firstOrNull { it.code == coinCode }?.let {
            addressLiveData.value = it.publicKey
        }
    }
}