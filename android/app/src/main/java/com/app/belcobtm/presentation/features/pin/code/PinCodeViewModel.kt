package com.app.belcobtm.presentation.features.pin.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.AuthorizeUseCase
import com.app.belcobtm.domain.authorization.interactor.GetAuthorizePinUseCase
import com.app.belcobtm.domain.authorization.interactor.SaveAuthorizePinUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class PinCodeViewModel(
    private val authorizeUseCase: AuthorizeUseCase,
    private val authorizePinUseCase: GetAuthorizePinUseCase,
    private val savePinCodeUseCase: SaveAuthorizePinUseCase
) : ViewModel() {
    val authorizationLiveData = MutableLiveData<LoadingData<Unit>>()
    var enteredCreatePinCode: String = ""
    var enteredPinCode: String = ""

    fun savePinCode(pinCode: String) = savePinCodeUseCase.invoke(SaveAuthorizePinUseCase.Params(pinCode))

    fun getSavedPinCode(): String = authorizePinUseCase.invoke()

    fun authorize() {
        authorizationLiveData.value = LoadingData.Loading()
        authorizeUseCase.invoke(
            Unit,
            onSuccess = { authorizationLiveData.value = LoadingData.Success(it) },
            onError = { authorizationLiveData.value = LoadingData.Error(it) }
        )
    }
}