package com.belcobtm.presentation.features.authorization.recover.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.authorization.interactor.AuthorizationCheckCredentialsUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.validator.Validator

class RecoverWalletViewModel(
    private val checkCredentialsUseCase: AuthorizationCheckCredentialsUseCase,
    private val phoneNumberValidator: Validator<String>
) : ViewModel() {

    val checkCredentialsLiveData: MutableLiveData<LoadingData<Triple<Boolean, Boolean, Boolean>>> = MutableLiveData()

    fun checkCredentials(phone: String, password: String, email: String) {
        checkCredentialsLiveData.value = LoadingData.Loading()
        checkCredentialsUseCase(
            AuthorizationCheckCredentialsUseCase.Params(phone, password, email),
            onSuccess = { checkCredentialsLiveData.value = LoadingData.Success(it) },
            onError = { checkCredentialsLiveData.value = LoadingData.Error(it) }
        )
    }

    fun isValidMobileNumber(phoneNumber: String): Boolean =
        phoneNumberValidator.isValid(phoneNumber)

}
