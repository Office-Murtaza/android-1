package com.app.belcobtm.presentation.features.atm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class AtmViewModel: ViewModel() {
    val stateData = MutableLiveData<LoadingData<AtmState>>(LoadingData.Success(AtmState(emptyList())))

    fun requestAtms() {

    }
}

data class AtmState(val atms: List<AtmItem>)