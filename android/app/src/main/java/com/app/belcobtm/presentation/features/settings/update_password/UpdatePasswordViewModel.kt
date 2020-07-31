package com.app.belcobtm.presentation.features.settings.update_password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.authorization.interactor.ChangePassUseCase

class UpdatePasswordViewModel(
    val changePassUseCase: ChangePassUseCase
) : ViewModel() {
    private var oldPass = ""
    private var newPass = ""
    private var newPassConfirm = ""

    val stateData = MutableLiveData<UpdatePasswordState>(UpdatePasswordState())
    val actionData = MutableLiveData<UpdatePasswordAction>()

    fun onOldPassTextChanged(text: String) {
        oldPass = text
        stateData.value = stateData.value!!.copy(
            isOldPasswordError = false,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        )
    }

    fun onNewPassTextChanged(text: String) {
        newPass = text
        stateData.value = stateData.value!!.copy(
            isNewPasswordMatches = newPass == newPassConfirm,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        )
    }

    fun onNewPassConfirmTextChanged(text: String) {
        newPassConfirm = text
        stateData.value = stateData.value!!.copy(
            isNewPasswordMatches = newPass == newPassConfirm,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        )
    }

    fun onNextClick() {
        stateData.value = stateData.value!!.copy(
            isLoading = true
        )
        changePassUseCase.invoke(
            ChangePassUseCase.Params(
                oldPassword = oldPass,
                newPassword = newPass
            ),
            onSuccess = {
                if (it) {
                    stateData.value = stateData.value!!.copy(isLoading = false)
                    actionData.value = UpdatePasswordAction.Success
                } else {
                    stateData.value = stateData.value!!.copy(isLoading = false)
                    actionData.value = UpdatePasswordAction.Failure
                }
            },
            onError = {
                actionData.value = UpdatePasswordAction.Failure
            }
        )
    }

    private fun isButtonEnabled(): Boolean {
        return oldPass.isNotEmpty()
                && newPass.isNotEmpty()
                && newPassConfirm.isNotEmpty()
                && newPass == newPassConfirm
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
    object Failure : UpdatePasswordAction()
}