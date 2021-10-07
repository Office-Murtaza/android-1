package com.belcobtm.presentation.features.sms.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.tools.interactor.SendSmsToDeviceUseCase
import com.belcobtm.domain.tools.interactor.VerifySmsCodeUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData

class SmsCodeViewModel(
    private val phone: String,
    private val smsCodeUseCase: SendSmsToDeviceUseCase,
    private val verifySmsCodeUseCase: VerifySmsCodeUseCase
) : ViewModel() {
    val smsLiveData: MutableLiveData<LoadingData<Boolean>> = MutableLiveData()
    val smsVerifyLiveData: MutableLiveData<LoadingData<Boolean>> = MutableLiveData()

    init {
        sendSmsToDevice()
    }

    fun sendSmsToDevice() {
        smsLiveData.value = LoadingData.Loading()
        smsCodeUseCase(
            params = SendSmsToDeviceUseCase.Params(phone),
            onSuccess = { smsLiveData.value = LoadingData.Success(it) },
            onError = { smsLiveData.value = LoadingData.Error(it) }
        )
    }

    fun verifyCode(code: String) {
        smsVerifyLiveData.value = LoadingData.Loading()
        verifySmsCodeUseCase(
            params = VerifySmsCodeUseCase.Params(phone, code),
            onSuccess = { smsVerifyLiveData.value = LoadingData.Success(it) },
            onError = { smsVerifyLiveData.value = LoadingData.Error(it) }
        )
    }
}
