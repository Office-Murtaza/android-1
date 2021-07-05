package com.belcobtm.presentation.features.authorization.create.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.authorization.interactor.AuthorizationCheckCredentialsUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.validator.Validator

class CreateWalletViewModel(
    private val checkCredentialsUseCase: AuthorizationCheckCredentialsUseCase,
    private val phoneNumberValidator: Validator<String>
) : ViewModel() {
    val checkCredentialsLiveData: MutableLiveData<LoadingData<Pair<Boolean, Boolean>>> = MutableLiveData()

    fun checkCredentials(phone: String, password: String) {
        checkCredentialsLiveData.value = LoadingData.Loading()
        checkCredentialsUseCase.invoke(
            AuthorizationCheckCredentialsUseCase.Params(phone, password),
            onSuccess = { checkCredentialsLiveData.value = LoadingData.Success(it) },
            onError = { checkCredentialsLiveData.value = LoadingData.Error(it) }
        )
    }

    fun isValidMobileNumber(phoneNumber: String): Boolean =
        phoneNumberValidator.isValid(phoneNumber)
}