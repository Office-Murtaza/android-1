package com.belcobtm.presentation.screens.settings.security.phone

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.Failure
import com.belcobtm.domain.PreferencesInteractor
import com.belcobtm.domain.settings.interactor.IsPhoneUsedUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import com.belcobtm.presentation.tools.extensions.getPhoneForRequest
import com.belcobtm.presentation.tools.validator.Validator

class UpdatePhoneViewModel(
    private val isPhoneUsedUseCase: IsPhoneUsedUseCase,
    private val preferences: PreferencesInteractor,
    private val phoneNumberValidator: Validator<String>
) : ViewModel() {

    val stateData =
        MutableLiveData<LoadingData<UpdatePhoneState>>(LoadingData.Success(UpdatePhoneState()))
    val actionData = SingleLiveData<UpdatePhoneAction>()
    private var phone = ""

    fun onPhoneInput(text: String) {
        phone = text.getPhoneForRequest()
        stateData.value = LoadingData.Success(UpdatePhoneState(isValidMobileNumber(phone)))
    }

    fun onNextClick() {
        if (phone == preferences.userPhone) {
            stateData.value = LoadingData.Error(
                data = stateData.value?.commonData,
                errorType = Failure.MessageError(code = ERROR_UPDATE_PHONE_IS_SAME, message = null)
            )
            return
        }
        verifyPhone()
    }

    private fun verifyPhone() {
        isPhoneUsedUseCase.invoke(
            IsPhoneUsedUseCase.Params(phone),
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
        actionData.value = UpdatePhoneAction.GoToSmsVerification(phone)
    }

    private fun isValidMobileNumber(phone: String): Boolean =
        phoneNumberValidator.isValid(phone)

    companion object {

        const val ERROR_UPDATE_PHONE_IS_USED = 2
        const val ERROR_UPDATE_PHONE_IS_SAME = 3
    }

}

data class UpdatePhoneState(
    val isNextButtonEnabled: Boolean = false,
    val isPhoneError: Boolean = false
)

sealed class UpdatePhoneAction {
    data class GoToSmsVerification(val phone: String) : UpdatePhoneAction()
}
