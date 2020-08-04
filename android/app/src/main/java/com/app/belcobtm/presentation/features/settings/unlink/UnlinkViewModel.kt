package com.app.belcobtm.presentation.features.settings.unlink

import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.UnlinkUseCase
import com.app.belcobtm.presentation.core.SingleLiveData
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class UnlinkViewModel(
    private val unlinkUseCase: UnlinkUseCase
) : ViewModel() {

    val actionData = SingleLiveData<LoadingData<Unit>>()

    fun unlink() {
        actionData.value = LoadingData.Loading()
        unlinkUseCase.invoke(Unit,
        onSuccess = {
            if (it) {
                actionData.value = LoadingData.Success(Unit)
            } else {
                actionData.value = LoadingData.Error()
            }
        },
        onError = {
            actionData.value = LoadingData.Error()
        })
    }
}