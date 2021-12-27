package com.belcobtm.domain.service

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.data.disk.database.service.ServiceType
import com.belcobtm.presentation.core.adapter.model.ListItem

data class ServiceItem(
    override val id: String,
    @DrawableRes val icon: Int,
    @ServiceType val serviceType: Int,
    @StringRes val title: Int,
) : ListItem {

    override val type: Int
        get() = SERVICE_ITEM_TYPE

    companion object {
        const val SERVICE_ITEM_TYPE = 1
    }
}