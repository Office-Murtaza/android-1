package com.belcobtm.domain.service

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.belcobtm.presentation.core.adapter.model.ListItem

data class ServiceItem(
    override val id: String,
    @DrawableRes val icon: Int,
    val serviceType: ServiceType,
    @StringRes val title: Int,
    var locationEnabled: Boolean = true,
    var verificationEnabled: Boolean = true,
    val feePercent: Double,
    val txLimit: Double,
    val dailyLimit: Double,
    val remainLimit: Double
) : ListItem {

    override val type: Int
        get() = SERVICE_ITEM_TYPE

    companion object {

        const val SERVICE_ITEM_TYPE = 1
    }

}
