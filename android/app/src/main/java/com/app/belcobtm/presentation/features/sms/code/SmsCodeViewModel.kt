package com.app.belcobtm.presentation.features.sms.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class SmsCodeViewModel(
    private val phone: String,
    private val smsCodeUseCase: SendSmsToDeviceUseCase
) : ViewModel() {
    val smsLiveData: MutableLiveData<LoadingData<String>> = MutableLiveData()

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
}