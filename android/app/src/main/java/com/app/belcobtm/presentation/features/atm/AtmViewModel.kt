package com.app.belcobtm.presentation.features.atm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.data.rest.atm.response.AtmResponse
import com.app.belcobtm.domain.atm.interactor.GetAtmsUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData
import com.google.android.gms.maps.model.LatLng

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