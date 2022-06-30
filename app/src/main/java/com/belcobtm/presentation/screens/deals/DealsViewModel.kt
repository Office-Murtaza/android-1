package com.belcobtm.presentation.screens.deals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.authorization.interactor.GetVerificationStatusUseCase
import com.belcobtm.domain.settings.type.VerificationStatus
import com.belcobtm.presentation.core.mvvm.LoadingData

class DealsViewModel(private val getVerificationInfoUseCase: GetVerificationStatusUseCase) :
    ViewModel() {

    val stateData = MutableLiveData<LoadingData<VerificationStatus>>()

    init {
        getVerificationInfoUseCase.invoke(Unit, onSuccess = {
            stateData.value = LoadingData.Success(
                it
            )
        }, onError = {

        })
    }
}