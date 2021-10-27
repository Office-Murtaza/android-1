package com.belcobtm.presentation.features.atm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData

class AtmViewModel(private val getAtmsUseCase: GetAtmsUseCase) : ViewModel() {

    val stateData = MutableLiveData<LoadingData<AtmsInfoItem>>(
        LoadingData.Success(
            AtmsInfoItem(emptyList(), null)
        )
    )

    fun requestAtms() {
        getAtmsUseCase.invoke(Unit,
            onSuccess = { atmItems ->
                stateData.value = LoadingData.Success(atmItems)
            },
            onError = { stateData.value = LoadingData.Error(it, stateData.value!!.commonData) })
    }
}