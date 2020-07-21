package com.app.belcobtm.presentation.features.authorization.recover.seed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class RecoverSeedViewModel(private val recoverWalletUseCase: RecoverWalletUseCase) : ViewModel() {
    val recoverWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun recoverWallet(seed: String, phone: String, password: String) {
        recoverWalletLiveData.value = LoadingData.Loading()
        recoverWalletUseCase.invoke(
            params = RecoverWalletUseCase.Params(seed, phone, password),
            onSuccess = { recoverWalletLiveData.value = LoadingData.Success(it) },
            onError = { recoverWalletLiveData.value = LoadingData.Error(it) }
        )
    }
}