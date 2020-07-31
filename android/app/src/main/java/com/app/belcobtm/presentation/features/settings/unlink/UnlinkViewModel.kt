package com.app.belcobtm.presentation.features.settings.unlink

import androidx.lifecycle.ViewModel
import com.app.belcobtm.data.disk.shared.preferences.SharedPreferencesHelper
import com.app.belcobtm.domain.authorization.interactor.UnlinkUseCase
import com.app.belcobtm.presentation.core.SingleLiveData

class UnlinkViewModel(
    private val unlinkUseCase: UnlinkUseCase
) : ViewModel() {

    val actionData = SingleLiveData<UnlinkAction>()

    fun unlink() {
        actionData.value = UnlinkAction.Loading
        unlinkUseCase.invoke(Unit,
        onSuccess = {
            if (it) {
                actionData.value = UnlinkAction.Success
            } else {
                actionData.value = UnlinkAction.Failure
            }
        },
        onError = {
            actionData.value = UnlinkAction.Failure
        })
    }
}

sealed class UnlinkAction {
    object Loading : UnlinkAction()
    object Success : UnlinkAction()
    object Failure : UnlinkAction()
}