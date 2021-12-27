package com.belcobtm.presentation.features.deals.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemDealsBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate

class DealsItemDelegate(
    private val onServiceClicked: (ServiceItem) -> Unit
) : AdapterDelegate<ServiceItem, DealsItemViewHolder>() {

    override val viewType: Int
        get() = ServiceItem.SERVICE_ITEM_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): DealsItemViewHolder =
        DealsItemViewHolder(
            ItemDealsBinding.inflate(inflater, parent, false),
            onServiceClicked
        )
}