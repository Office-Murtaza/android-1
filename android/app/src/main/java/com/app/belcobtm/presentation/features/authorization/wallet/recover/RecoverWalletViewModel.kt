package com.app.belcobtm.presentation.features.authorization.wallet.recover

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.RecoverWalletUseCase
import com.app.belcobtm.domain.authorization.interactor.RecoverWalletVerifySmsCodeUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class RecoverWalletViewModel(
    private val recoverWalletUseCase: RecoverWalletUseCase,
    private val smsCodeUseCase: RecoverWalletVerifySmsCodeUseCase
) : ViewModel() {
    val recoverWalletLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()
    val smsCodeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun recoverWallet(phone: String, password: String) {
        recoverWalletLiveData.value = LoadingData.Loading()
        recoverWalletUseCase.invoke(
            RecoverWalletUseCase.Params(phone, password),
            onSuccess = { recoverWalletLiveData.value = LoadingData.Success(Unit) },
            onError = { recoverWalletLiveData.value = LoadingData.Error(it) }
        )
    }

    fun verifySmsCode(smsCode: String) {
        smsCodeLiveData.value = LoadingData.Loading()
        smsCodeUseCase.invoke(
            RecoverWalletVerifySmsCodeUseCase.Params(smsCode),
            onSuccess = { smsCodeLiveData.value = LoadingData.Success(Unit) },
            onError = { smsCodeLiveData.value = LoadingData.Error(it) }
        )
    }
}