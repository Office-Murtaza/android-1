package com.belcobtm.presentation.features.contacts.adapter.holder

import com.belcobtm.databinding.ItemContactHeaderBinding
import com.belcobtm.domain.contacts.item.ContactHeader
import com.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class ContactHeaderViewHolder(
    private val binding: ItemContactHeaderBinding
) : MultiTypeViewHolder<ContactHeader>(binding.root) {

    override fun bind(model: ContactHeader) {
        binding.title.text = model.title
    }
}