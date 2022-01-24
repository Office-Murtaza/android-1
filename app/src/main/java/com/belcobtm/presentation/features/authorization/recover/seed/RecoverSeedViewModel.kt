package com.belcobtm.presentation.features.authorization.recover.seed

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.belcobtm.domain.settings.interactor.SetNeedToShowRestrictionsUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData

class RecoverSeedViewModel(
    private val recoverWalletUseCase: RecoverWalletUseCase,
    private val setNeedToShowRestrictionsUseCase: SetNeedToShowRestrictionsUseCase
) : ViewModel() {
    val recoverWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun recoverWallet(seed: String, phone: String, password: String) {
        recoverWalletLiveData.value = LoadingData.Loading()
        recoverWalletUseCase.invoke(
            params = RecoverWalletUseCase.Params(seed, phone, password),
            onSuccess = {
                setNeedToShowRestrictionsUseCase.invoke(SetNeedToShowRestrictionsUseCase.Params(true))
                recoverWalletLiveData.value = LoadingData.Success(it)
            },
            onError = {
                recoverWalletLiveData.value = LoadingData.Error(it)
            }
        )
    }
}