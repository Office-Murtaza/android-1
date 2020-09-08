package com.app.belcobtm.presentation.features.sms.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.data.rest.settings.request.UpdatePhoneParam
import com.app.belcobtm.domain.settings.interactor.UpdatePhoneUseCase
import com.app.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SmsCodeViewModel(
    private val phone: String,
    private val smsCodeUseCase: SendSmsToDeviceUseCase,
    private val phoneChangeUseCase: UpdatePhoneUseCase
) : ViewModel() {
    val smsLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()
    val phoneUpdateData: SingleLiveData<LoadingData<Boolean>> = SingleLiveData()

    init {
        sendSmsToDevice()
    }

    fun sendSmsToDevice() {
        smsLiveData.value = LoadingData.Loading()
        smsCodeUseCase.invoke(
            params = SendSmsToDeviceUseCase.Params(phone),
            onSuccess = { smsLiveData.value = LoadingData.Success(it) },
            onError = { smsLiveData.value = LoadingData.Error(it) }
        )
    }

    fun changePhone() {
        phoneChangeUseCase.invoke(
            UpdatePhoneUseCase.Params(phone),
            onSuccess = {
                phoneUpdateData.value = LoadingData.Success(true)
            },
            onError = {
                phoneUpdateData.value = LoadingData.Error(errorType = it, data = phoneUpdateData.value?.commonData)
            }
        )
    }
}