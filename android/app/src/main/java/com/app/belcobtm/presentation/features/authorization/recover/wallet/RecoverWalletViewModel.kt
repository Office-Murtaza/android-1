package com.app.belcobtm.presentation.features.authorization.recover.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.AuthorizationCheckCredentialsUseCase
import com.app.belcobtm.domain.authorization.interactor.AuthorizationVerifySmsCodeUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class RecoverWalletViewModel(
    private val checkCredentialsUseCase: AuthorizationCheckCredentialsUseCase,
    private val smsCodeUseCase: AuthorizationVerifySmsCodeUseCase
) : ViewModel() {
    val checkCredentialsLiveData: MutableLiveData<LoadingData<Boolean>> = MutableLiveData()
    val smsCodeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun checkCredentials(phone: String, password: String) {
        checkCredentialsLiveData.value = LoadingData.Loading()
        checkCredentialsUseCase.invoke(
            AuthorizationCheckCredentialsUseCase.Params(phone, password),
            onSuccess = { checkCredentialsLiveData.value = LoadingData.Success(it) },
            onError = { checkCredentialsLiveData.value = LoadingData.Error(it) }
        )
    }

    fun verifySmsCode(smsCode: String) {
        smsCodeLiveData.value = LoadingData.Loading()
        smsCodeUseCase.invoke(
            AuthorizationVerifySmsCodeUseCase.Params(smsCode),
            onSuccess = { smsCodeLiveData.value = LoadingData.Success(Unit) },
            onError = { smsCodeLiveData.value = LoadingData.Error(it) }
        )
    }
}