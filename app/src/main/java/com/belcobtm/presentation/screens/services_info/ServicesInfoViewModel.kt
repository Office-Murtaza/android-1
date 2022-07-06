package com.belcobtm.presentation.screens.services_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.belcobtm.domain.service.ServiceInfoProvider
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.domain.service.ServiceType
import kotlinx.coroutines.launch

class ServicesInfoViewModel(
    private val serviceInfoProvider: ServiceInfoProvider
) : ViewModel() {

    private val _serviceLiveData = MutableLiveData<ServiceItem>()
    val serviceLiveData: LiveData<ServiceItem> = _serviceLiveData

    fun getService(type: ServiceType) = viewModelScope.launch {
        _serviceLiveData.value = serviceInfoProvider.getService(type)
    }

}