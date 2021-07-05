package com.belcobtm.presentation.features.atm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.data.rest.atm.response.AtmResponse
import com.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.belcobtm.presentation.core.mvvm.LoadingData

class AtmViewModel(private val getAtmsUseCase: GetAtmsUseCase) : ViewModel() {
    val stateData =
        MutableLiveData<LoadingData<List<AtmResponse.AtmAddress>>>(LoadingData.Success(emptyList()))

    fun requestAtms() {
        getAtmsUseCase.invoke(Unit,
            onSuccess = { atmResponse ->
                stateData.value = LoadingData.Success(atmResponse.addresses)
            },
            onError = { stateData.value = LoadingData.Error(it, stateData.value!!.commonData) })
    }
}