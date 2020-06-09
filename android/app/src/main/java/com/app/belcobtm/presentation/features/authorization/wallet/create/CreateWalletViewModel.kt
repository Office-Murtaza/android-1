package com.app.belcobtm.presentation.features.authorization.wallet.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.CreateWalletUseCase
import com.app.belcobtm.domain.authorization.interactor.CreateWalletVerifySmsCodeUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class CreateWalletViewModel(
    private val createWalletUseCase: CreateWalletUseCase,
    private val createWalletVerifySmsCodeUseCase: CreateWalletVerifySmsCodeUseCase
) : ViewModel() {
    val createWalletLiveData = MutableLiveData<LoadingData<Unit>>()
    val smsCodeLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()

    fun createWallet(phone: String, password: String) {
        createWalletLiveData.value = LoadingData.Loading()
        createWalletUseCase.invoke(
            CreateWalletUseCase.Params(phone, password),
            onSuccess = { createWalletLiveData.value = LoadingData.Success(it) },
            onError = { createWalletLiveData.value = LoadingData.Error(it) }
        )
    }

    fun verifySmsCode(smsCode: String) {
        smsCodeLiveData.value = LoadingData.Loading()
        createWalletVerifySmsCodeUseCase.invoke(CreateWalletVerifySmsCodeUseCase.Params(smsCode),
            onSuccess = { smsCodeLiveData.value = LoadingData.Success(it) },
            onError = { smsCodeLiveData.value = LoadingData.Error(it) }
        )
    }
}