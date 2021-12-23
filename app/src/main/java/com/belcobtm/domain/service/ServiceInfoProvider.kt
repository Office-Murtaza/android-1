package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType

class ServiceInfoProvider(
    private val serviceRepository: ServiceRepository
) {

    fun isAvailableService(@ServiceType serviceType: Int) =
        serviceRepository.isAvailable(serviceType)

    fun getService(@ServiceType serviceType: Int): ServiceEntity? =
        serviceRepository.getService(serviceType)
}