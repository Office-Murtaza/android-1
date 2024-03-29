package com.belcobtm.presentation.screens.settings.security.update_password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.Failure
import com.belcobtm.domain.settings.interactor.CHANGE_PASS_ERROR_OLD_PASS
import com.belcobtm.domain.settings.interactor.ChangePassUseCase
import com.belcobtm.presentation.core.Const.MAX_PASS
import com.belcobtm.presentation.core.Const.MIN_PASS
import com.belcobtm.presentation.core.mvvm.LoadingData

const val ERROR_PASSWORDS_SAME = 1

class UpdatePasswordViewModel(
    val changePassUseCase: ChangePassUseCase
) : ViewModel() {
    private var oldPass = ""
    private var newPass = ""
    private var newPassConfirm = ""
    private var startValidation = false

    val stateData = MutableLiveData<LoadingData<UpdatePasswordState>>(LoadingData.Success(UpdatePasswordState()))
    val actionData = MutableLiveData<UpdatePasswordAction>()

    fun onOldPassTextChanged(text: String) {
        oldPass = text
        stateData.value = LoadingData.Success(stateData.value!!.commonData!!.copy(
            isOldPasswordError = false,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        ))
    }

    fun onNewPassTextChanged(text: String) {
        newPass = text
        stateData.value = LoadingData.Success(
            stateData.value?.commonData?.copy(
                isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            )?: UpdatePasswordState(
                isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            )
        )
    }

    fun onNewPassConfirmTextChanged(text: String) {
        newPassConfirm = text
        stateData.value = LoadingData.Success(stateData.value?.commonData?.copy(
            isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        )?: UpdatePasswordState(
            isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        ))
    }

    fun onNextClick() {
        if (!startValidation) {
            startValidation = true
        }
        if (newPassConfirm != newPass) {
            stateData.value = LoadingData.Success(stateData.value?.commonData?.copy(
                isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            )?: UpdatePasswordState(
                isNewPasswordMatches = newPass == newPassConfirm || !startValidation,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            ))
            return
        }
        if (oldPass == newPass) {
            stateData.value = LoadingData.Error(data = stateData.value?.commonData, errorType = Failure.MessageError(code = ERROR_PASSWORDS_SAME, message = null))
            return
        }
        stateData.value = LoadingData.Loading(stateData.value?.commonData)
        changePassUseCase.invoke(
            ChangePassUseCase.Params(
                oldPassword = oldPass,
                newPassword = newPass
            ),
            onSuccess = {
                if (it) {
                    actionData.value = UpdatePasswordAction.Success
                } else {
                    stateData.value = LoadingData.Error(data = stateData.value?.commonData)
                }
            },
            onError = {
                if (it is Failure.MessageError && it.code == CHANGE_PASS_ERROR_OLD_PASS) {
                    stateData.value = LoadingData.Success(stateData.value?.commonData?.copy(isOldPasswordError = true)?: UpdatePasswordState(isOldPasswordError = true))
                } else {
                    stateData.value = LoadingData.Error(data = stateData.value?.commonData, errorType = it)
                }
            }
        )
    }

    private fun isButtonEnabled(): Boolean {
        return oldPass.isNotEmpty()
                && newPass.length in MIN_PASS..MAX_PASS
                && newPassConfirm.isNotEmpty()
                && (!startValidation || (newPass == newPassConfirm))
    }
}

data class UpdatePasswordState(
    val isOldPasswordError: Boolean = false,
    val isNewPasswordMatches: Boolean = true,
    val isNextButtonEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed class UpdatePasswordAction {
    object Success : UpdatePasswordAction()
}