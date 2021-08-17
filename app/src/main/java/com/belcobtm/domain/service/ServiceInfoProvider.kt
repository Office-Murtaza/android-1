package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceType

class ServiceInfoProvider(
    private val serviceRepository: ServiceRepository
) {

    fun isAvailableService(@ServiceType serviceType: Int) =
        serviceRepository.isAvailable(serviceType)

    fun getServiceFee(@ServiceType serviceType: Int) =
        serviceRepository.getServiceFee(serviceType)
}