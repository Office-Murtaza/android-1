package com.belcobtm.presentation.screens.services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceItem
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ServicesViewModel(
    availabilityProvider: ServiceInfoProvider
) : ViewModel() {

    private val _servicesLiveData = MutableLiveData<List<ServiceItem>>()
    val servicesLiveData: LiveData<List<ServiceItem>> = _servicesLiveData

    init {
        availabilityProvider.observeServices().onEach {
            _servicesLiveData.value = it
        }.launchIn(viewModelScope)
    }

}
