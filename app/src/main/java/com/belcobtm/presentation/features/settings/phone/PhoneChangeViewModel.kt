package com.belcobtm.presentation.features.settings.phone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.interactor.VerifyPhoneUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.core.validator.Validator

class PhoneChangeViewModel(
    private val verifyPhoneUseCase: VerifyPhoneUseCase,
    private val prefsHelper: SharedPreferencesHelper,
    private val phoneNumberValidator: Validator<String>
) : ViewModel() {

    val stateData =
        MutableLiveData<LoadingData<PhoneChangeState>>(LoadingData.Success(PhoneChangeState()))
    val actionData = SingleLiveData<PhoneChangeAction>()
    private var phone = ""

    fun onPhoneInput(text: String) {
        phone = text
        stateData.value = LoadingData.Success(PhoneChangeState(isValidMobileNumber(phone)))
    }

    fun onNextClick() {
        if (phone == prefsHelper.userPhone) {
            stateData.value = LoadingData.Error(
                data = stateData.value?.commonData,
                errorType = Failure.MessageError(code = ERROR_UPDATE_PHONE_IS_SAME, message = null)
            )
            return
        }
        verifyPhone()
    }

    private fun verifyPhone() {
        verifyPhoneUseCase.invoke(
            VerifyPhoneUseCase.Params(phone),
            onSuccess = {
                if (it) {
                    stateData.value = LoadingData.Error(
                        data = stateData.value?.commonData,
                        errorType = Failure.MessageError(
                            code = ERROR_UPDATE_PHONE_IS_USED,
                            message = null
                        )
                    )
                } else {
                    goToSmsVerification()
                }
            },
            onError = {
                stateData.value =
                    LoadingData.Error(data = stateData.value?.commonData, errorType = it)
            })
    }

    private fun goToSmsVerification() {
        actionData.value = PhoneChangeAction.GoToSmsVerification(phone)
    }

    private fun isValidMobileNumber(phone: String): Boolean =
        phoneNumberValidator.isValid(phone)

    companion object {

        const val ERROR_UPDATE_PHONE_IS_USED = 2
        const val ERROR_UPDATE_PHONE_IS_SAME = 3
    }

}

data class PhoneChangeState(
    val isNextButtonEnabled: Boolean = false,
    val isPhoneError: Boolean = false
)

sealed class PhoneChangeAction {
    data class GoToSmsVerification(val phone: String) : PhoneChangeAction()
}
