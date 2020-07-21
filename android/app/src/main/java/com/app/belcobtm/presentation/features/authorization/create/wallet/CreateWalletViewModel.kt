package com.app.belcobtm.presentation.features.authorization.create.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.AuthorizationCheckCredentialsUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class CreateWalletViewModel(
    private val checkCredentialsUseCase: AuthorizationCheckCredentialsUseCase
) : ViewModel() {
    val checkCredentialsLiveData: MutableLiveData<LoadingData<Pair<Boolean, Boolean>>> = MutableLiveData()
    val smsCodeLiveData: MutableLiveData<LoadingData<Unit>> = MutableLiveData()

    fun checkCredentials(phone: String, password: String) {
        checkCredentialsLiveData.value = LoadingData.Loading()
        checkCredentialsUseCase.invoke(
            AuthorizationCheckCredentialsUseCase.Params(phone, password),
            onSuccess = { checkCredentialsLiveData.value = LoadingData.Success(it) },
            onError = { checkCredentialsLiveData.value = LoadingData.Error(it) }
        )
    }
}