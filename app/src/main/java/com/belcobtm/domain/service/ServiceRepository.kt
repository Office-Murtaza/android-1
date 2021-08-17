package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType

interface ServiceRepository {

    fun prefetchServices()

    suspend fun updateServices(availableServices: List<ServiceEntity>)

    fun isAvailable(@ServiceType serviceType: Int): Boolean

    fun getServiceFee(@ServiceType serviceType: Int): Double
}