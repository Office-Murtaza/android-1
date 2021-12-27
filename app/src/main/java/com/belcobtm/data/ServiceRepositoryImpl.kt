package com.belcobtm.data

import android.util.Log
import com.belcobtm.data.disk.database.service.ServiceDao
import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse
import com.belcobtm.data.websockets.services.model.toEntity
import com.belcobtm.domain.service.ServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.lang.Exception

class ServiceRepositoryImpl(
    private val serviceDao: ServiceDao,
    private val serviceScope: CoroutineScope
) : ServiceRepository {

    private val serviceInMemoryCache = MutableStateFlow<List<ServiceEntity>>(emptyList())

    override fun prefetchServices() {
        serviceScope.launch {
            serviceDao.observeAvailable()
                .collect(serviceInMemoryCache::emit)
        }
    }

    override fun observeServices(): Flow<List<ServiceEntity>> =
        serviceInMemoryCache

    override suspend fun updateServices(services: List<ServicesInfoResponse>) {
        services.sortedBy(ServicesInfoResponse::index)
            .mapNotNull(ServicesInfoResponse::toEntity)
            .let(serviceDao::updateServices)
    }

    override fun isAvailable(serviceType: Int): Boolean =
        serviceInMemoryCache.value.find { it.id == serviceType } != null

    override fun getService(serviceType: Int): ServiceEntity? =
        serviceInMemoryCache.value.find { it.id == serviceType }

}