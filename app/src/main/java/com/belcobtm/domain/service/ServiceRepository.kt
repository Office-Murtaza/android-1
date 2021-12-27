package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse
import kotlinx.coroutines.flow.Flow

interface ServiceRepository {

    fun prefetchServices()

    fun observeServices(): Flow<List<ServiceEntity>>

    suspend fun updateServices(services: List<ServicesInfoResponse>)

    fun isAvailable(@ServiceType serviceType: Int): Boolean

    fun getService(@ServiceType serviceType: Int): ServiceEntity?
}