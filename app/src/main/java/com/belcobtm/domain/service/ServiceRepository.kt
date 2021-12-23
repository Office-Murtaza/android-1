package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse

interface ServiceRepository {

    fun prefetchServices()

    suspend fun updateServices(services: List<ServicesInfoResponse>)

    fun isAvailable(@ServiceType serviceType: Int): Boolean

    fun getService(@ServiceType serviceType: Int): ServiceEntity?
}