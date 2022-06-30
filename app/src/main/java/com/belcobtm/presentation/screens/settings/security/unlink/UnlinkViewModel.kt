package com.belcobtm.presentation.screens.settings.security.unlink

import androidx.lifecycle.ViewModel
import com.belcobtm.domain.settings.interactor.UnlinkUseCase
import com.belcobtm.domain.socket.DisconnectFromSocketUseCase
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData

class UnlinkViewModel(
    private val unlinkUseCase: UnlinkUseCase,
    private val disconnectFromSocketUseCase: DisconnectFromSocketUseCase
) : ViewModel() {

    val actionData = SingleLiveData<LoadingData<Unit>>()

    fun unlink() {
        actionData.value = LoadingData.Loading()
        disconnectFromSocketUseCase(Unit)
        unlinkUseCase.invoke(Unit,
            onSuccess = {
                actionData.value = LoadingData.Success(Unit)
            },
            onError = {
                actionData.value = LoadingData.Error(errorType = it)
            })
    }
}