package com.app.belcobtm.presentation.features.contacts.adapter.holder

import com.app.belcobtm.databinding.ItemContactHeaderBinding
import com.app.belcobtm.domain.contacts.item.ContactHeader
import com.app.belcobtm.presentation.core.adapter.holder.MultiTypeViewHolder

class ContactHeaderViewHolder(
    private val binding: ItemContactHeaderBinding
) : MultiTypeViewHolder<ContactHeader>(binding.root) {

    override fun bind(model: ContactHeader) {
        binding.title.text = model.title
    }
}