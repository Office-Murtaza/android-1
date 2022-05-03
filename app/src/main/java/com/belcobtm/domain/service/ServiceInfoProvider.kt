package com.belcobtm.domain.service

import com.belcobtm.R
import com.belcobtm.data.disk.database.service.ServiceEntity
import com.belcobtm.data.disk.database.service.ServiceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ServiceInfoProvider(
    private val serviceRepository: ServiceRepository
) {

    fun observeServices(): Flow<List<ServiceItem>> =
        serviceRepository.observeServices()
            .map(::mapServiceToItem)

    fun isAvailableService(@ServiceType serviceType: Int) =
        serviceRepository.isAvailable(serviceType)

    fun getService(@ServiceType serviceType: Int): ServiceEntity? =
        serviceRepository.getService(serviceType)

    private fun mapServiceToItem(entity: List<ServiceEntity>): List<ServiceItem> =
        entity.mapNotNull {
            val id = it.id.toString()
            when (it.id) {
                ServiceType.STAKING ->
                    ServiceItem(
                        id,
                        R.drawable.ic_staking,
                        it.id,
                        R.string.deals_staking,
                        it.locationEnabled,
                        it.verificationEnabled
                    )
                ServiceType.SWAP ->
                    ServiceItem(
                        id,
                        R.drawable.ic_swap,
                        it.id,
                        R.string.deals_swap,
                        it.locationEnabled,
                        it.verificationEnabled
                    )
                ServiceType.TRADE ->
                    ServiceItem(
                        id,
                        R.drawable.ic_trade,
                        it.id,
                        R.string.deals_trade,
                        it.locationEnabled,
                        it.verificationEnabled
                    )
                ServiceType.TRANSFER ->
                    ServiceItem(
                        id,
                        R.drawable.ic_transfer,
                        it.id,
                        R.string.deals_transfer,
                        it.locationEnabled,
                        it.verificationEnabled
                    )
                ServiceType.ATM_SELL ->
                    ServiceItem(
                        id,
                        R.drawable.ic_atm_sell,
                        it.id,
                        R.string.atm_sell_title,
                        it.locationEnabled,
                        it.verificationEnabled
                    )
                else -> null
            }
        }
}