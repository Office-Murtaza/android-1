package com.belcobtm.domain.service

import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.data.rest.service.ServiceFeeResponse

interface ServiceRepository {

    fun prefetchServices()

    suspend fun updateServices(services: List<Int>, fees: List<ServiceFeeResponse>)

    fun isAvailable(@ServiceType serviceType: Int): Boolean

    fun getServiceFee(@ServiceType serviceType: Int): Double
}