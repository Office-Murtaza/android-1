package com.belcobtm.domain.service

import com.belcobtm.data.websockets.services.model.ServicesInfoResponse
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {

    fun observeServices(): Flow<List<ServiceItem>>

    suspend fun updateServices(services: List<ServicesInfoResponse>)

    fun getService(serviceType: ServiceType): ServiceItem?

}
