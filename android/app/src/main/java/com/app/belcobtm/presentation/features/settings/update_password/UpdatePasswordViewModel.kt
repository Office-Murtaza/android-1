package com.app.belcobtm.presentation.features.settings.update_password

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.ChangePassUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class UpdatePasswordViewModel(
    val changePassUseCase: ChangePassUseCase
) : ViewModel() {
    private var oldPass = ""
    private var newPass = ""
    private var newPassConfirm = ""

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
                isNewPasswordMatches = newPass == newPassConfirm,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            )?: UpdatePasswordState(
                isNewPasswordMatches = newPass == newPassConfirm,
                isNextButtonEnabled = isButtonEnabled(),
                isLoading = false
            )
        )
    }

    fun onNewPassConfirmTextChanged(text: String) {
        newPassConfirm = text
        stateData.value = LoadingData.Success(stateData.value?.commonData?.copy(
            isNewPasswordMatches = newPass == newPassConfirm,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        )?: UpdatePasswordState(
            isNewPasswordMatches = newPass == newPassConfirm,
            isNextButtonEnabled = isButtonEnabled(),
            isLoading = false
        ))
    }

    fun onNextClick() {
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
                stateData.value = LoadingData.Error(data = stateData.value?.commonData)
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
}