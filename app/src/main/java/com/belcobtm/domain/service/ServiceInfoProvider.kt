package com.belcobtm.domain.service

import kotlinx.coroutines.flow.Flow

class ServiceInfoProvider(
    private val serviceRepository: ServiceRepository
) {

    fun observeServices(): Flow<List<ServiceItem>> =
        serviceRepository.observeServices()

    suspend fun getService(serviceType: ServiceType): ServiceItem? =
        serviceRepository.getService(serviceType)

}
