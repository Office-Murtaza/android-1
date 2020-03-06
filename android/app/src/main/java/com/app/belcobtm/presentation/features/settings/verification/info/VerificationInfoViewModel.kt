package com.app.belcobtm.presentation.features.settings.verification.info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.settings.interactor.GetVerificationInfoUseCase
import com.app.belcobtm.domain.settings.item.VerificationInfoDataItem
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class VerificationInfoViewModel(
    private val getVerificationInfoUseCase: GetVerificationInfoUseCase
) : ViewModel() {
    val verificationInfoLiveData = MutableLiveData<LoadingData<VerificationInfoDataItem>>()

    init {
        verificationInfoLiveData.value = LoadingData.Loading()
        getVerificationInfoUseCase.invoke(Unit) { either ->
            either.either(
                { verificationInfoLiveData.value = LoadingData.Error(it) },
                { verificationInfoLiveData.value = LoadingData.Success(it) }
            )
        }
    }
}