package com.belcobtm.presentation.features.deals.delegate

import androidx.core.content.ContextCompat
import com.belcobtm.databinding.ItemDealsBinding
import com.belcobtm.domain.service.ServiceItem
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class DealsItemViewHolder(
    private val binding: ItemDealsBinding,
    onServiceClicked: (ServiceItem) -> Unit,
) : MultiTypeViewHolder<ServiceItem>(binding.root) {

    init {
        binding.itemDeals.setOnClickListener {
            onServiceClicked(model)
        }
    }

    override fun bind(model: ServiceItem) {
        binding.itemDeals.setImage(ContextCompat.getDrawable(binding.root.context, model.icon))
        binding.itemDeals.setLabel(binding.root.context.getString(model.title))
    }
}