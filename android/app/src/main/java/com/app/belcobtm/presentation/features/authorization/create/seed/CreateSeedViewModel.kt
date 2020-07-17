package com.app.belcobtm.presentation.features.authorization.create.seed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.CreateSeedUseCase
import com.app.belcobtm.domain.authorization.interactor.CreateWalletUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class CreateSeedViewModel(
    private val createPhraseUseCase: CreateSeedUseCase,
    private val createWalletUseCase: CreateWalletUseCase
) : ViewModel() {
    val seedLiveData: MutableLiveData<String> = MutableLiveData()
    val createWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    init {
        createPhraseUseCase.invoke(
            params = Unit,
            onSuccess = { seedLiveData.value = it }
        )
    }

    fun createWallet(phone: String, password: String) {
        createWalletLiveData.value = LoadingData.Loading()
        createWalletUseCase.invoke(
            params = CreateWalletUseCase.Params(phone, password),
            onSuccess = { createWalletLiveData.value = LoadingData.Success(it) },
            onError = { createWalletLiveData.value = LoadingData.Error(it) }
        )
    }
}