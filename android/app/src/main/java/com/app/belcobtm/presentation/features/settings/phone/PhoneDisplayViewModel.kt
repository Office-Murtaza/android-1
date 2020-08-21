package com.app.belcobtm.presentation.features.settings.phone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.settings.interactor.GetPhoneUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class PhoneDisplayViewModel(private val getPhoneUseCase: GetPhoneUseCase, private val sharedPreferencesHelper: SharedPreferencesHelper) : ViewModel() {
    val stateData = MutableLiveData<LoadingData<PhoneDisplayState>>(LoadingData.Loading())

    init {
        getPhone()
    }

    fun getPhone() {
        getPhoneUseCase.invoke(Unit,
            onSuccess = {
                sharedPreferencesHelper.userPhone = it
                stateData.value =
                    LoadingData.Success(PhoneDisplayState(phone = it, isNextButtonEnabled = true))
            },
            onError = {
                stateData.value = LoadingData.Error()
            })
    }
}

data class PhoneDisplayState(
    val phone: String = "",
    val isNextButtonEnabled: Boolean = false
)