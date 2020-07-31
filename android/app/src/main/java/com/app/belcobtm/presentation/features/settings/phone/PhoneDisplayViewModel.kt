package com.app.belcobtm.presentation.features.settings.phone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.GetPhoneUseCase

class PhoneDisplayViewModel(private val getPhoneUseCase: GetPhoneUseCase) : ViewModel() {
    val stateData = MutableLiveData<PhoneDisplayState>(PhoneDisplayState.Loading)

    init {
        getPhoneUseCase.invoke(Unit,
            onSuccess = {
                stateData.value = PhoneDisplayState.Ready(phone = it, isNextButtonEnabled = true)
            },
            onError = {
                stateData.value = PhoneDisplayState.Error
            })
    }
}

sealed class PhoneDisplayState {
    object Loading : PhoneDisplayState()
    data class Ready(
        val phone: String = "",
        val isNextButtonEnabled: Boolean = false
    ) : PhoneDisplayState()
    object Error : PhoneDisplayState()
}