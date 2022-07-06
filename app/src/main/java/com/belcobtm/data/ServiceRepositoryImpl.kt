package com.belcobtm.data

import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceDao
import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.websockets.services.model.ServicesInfoResponse
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.domain.service.ServiceRepository
import com.belcobtm.domain.service.ServiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceRepositoryImpl(
    private val serviceDao: ServiceDao
) : ServiceRepository {

    override fun observeServices(): Flow<List<ServiceItem>> =
        serviceDao.getServicesFlow().map {
            mapServiceToItem(it)
        }

    override suspend fun updateServices(services: List<ServicesInfoResponse>) {
        services.sortedBy(ServicesInfoResponse::index)
            .mapNotNull(ServicesInfoResponse::toEntity)
            .let(serviceDao::updateServices)
    }

    override suspend fun getService(serviceType: ServiceType): ServiceItem? =
        serviceDao.getServiceByType(serviceType.value)?.mapToData()

    private fun mapServiceToItem(entity: List<ServiceEntity>): List<ServiceItem> = entity.mapNotNull { it.mapToData() }

    private fun ServiceEntity.mapToData(): ServiceItem? {
        return ServiceType.values().firstOrNull { type ->
            type.value == id
        }?.let { type ->
            ServiceItem(
                id = id.toString(),
                icon = getServiceIcon(type),
                serviceType = type,
                title = getServiceTitle(type),
                locationEnabled = locationEnabled,
                verificationEnabled = verificationEnabled,
                feePercent = feePercent,
                txLimit = txLimit,
                dailyLimit = dailyLimit,
                remainLimit = remainLimit
            )
        }
    }

    private fun getServiceIcon(type: ServiceType) = when (type) {
        ServiceType.TRANSFER -> R.drawable.ic_transfer
        ServiceType.SWAP -> R.drawable.ic_swap
        ServiceType.STAKING -> R.drawable.ic_staking
        ServiceType.TRADE -> R.drawable.ic_trade
        ServiceType.ATM_SELL -> R.drawable.ic_atm_sell
    }

    private fun getServiceTitle(type: ServiceType) = when (type) {
        ServiceType.TRANSFER -> R.string.deals_transfer
        ServiceType.SWAP -> R.string.deals_swap
        ServiceType.STAKING -> R.string.deals_staking
        ServiceType.TRADE -> R.string.deals_trade
        ServiceType.ATM_SELL -> R.string.atm_sell_title
    }

}
