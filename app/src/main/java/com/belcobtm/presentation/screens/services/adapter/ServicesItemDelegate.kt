package com.belcobtm.presentation.screens.services.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemServiceBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate

class ServicesItemDelegate(
    private val onServiceClicked: (ServiceItem) -> Unit,
    private val onVerifyClicked: () -> Unit
) : AdapterDelegate<ServiceItem, ServiceItemViewHolder>() {

    override val viewType: Int
        get() = ServiceItem.SERVICE_ITEM_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): ServiceItemViewHolder =
        ServiceItemViewHolder(
            ItemServiceBinding.inflate(inflater, parent, false),
            onServiceClicked,
            onVerifyClicked,
        )

}
