package com.app.belcobtm.presentation.features.authorization.pin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.App
import com.app.belcobtm.domain.authorization.interactor.AuthorizeUseCase
import com.app.belcobtm.domain.authorization.interactor.GetAuthorizePinUseCase
import com.app.belcobtm.domain.authorization.interactor.SaveAuthorizePinUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class PinViewModel(
    private val authorizeUseCase: AuthorizeUseCase,
    private val authorizePinUseCase: GetAuthorizePinUseCase,
    private val savePinCodeUseCase: SaveAuthorizePinUseCase
) : ViewModel() {
    val authorizationLiveData = MutableLiveData<LoadingData<Unit>>()

    fun setPinCode(pinCode: String) = savePinCodeUseCase.invoke(SaveAuthorizePinUseCase.Params(pinCode))

    fun getSavedPinCode(): String = authorizePinUseCase.invoke()

    fun authorize() {
        authorizationLiveData.value = LoadingData.Loading()
        authorizeUseCase.invoke(Unit) { either ->
            either.either(
                { authorizationLiveData.value = LoadingData.Error(it) },
                { authorizationLiveData.value = LoadingData.Success(it) }
            )
        }
    }
}