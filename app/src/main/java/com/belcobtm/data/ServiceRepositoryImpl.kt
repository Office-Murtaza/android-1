package com.belcobtm.data

import com.belcobtm.data.disk.database.service.ServiceDao
import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.domain.service.ServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

    override suspend fun updateServices(availableServices: List<ServiceEntity>) {
        serviceDao.updateServices(availableServices)
    }

    override fun isAvailable(serviceType: Int): Boolean =
        serviceInMemoryCache.value.find { it.id == serviceType } != null

    override fun getServiceFee(serviceType: Int): Double =
        serviceInMemoryCache.value.find { it.id == serviceType }
            ?.feePercent ?: throw IllegalArgumentException("Service is not available")


}