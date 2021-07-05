package com.belcobtm.presentation.features.sms.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData

class SmsCodeViewModel(
    private val phone: String,
    private val smsCodeUseCase: SendSmsToDeviceUseCase
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
}
