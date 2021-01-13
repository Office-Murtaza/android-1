package com.app.belcobtm.presentation.features.contacts.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemContactHeaderBinding
import com.app.belcobtm.domain.contacts.item.ContactHeader
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.contacts.adapter.holder.ContactHeaderViewHolder

class ContactHeaderDelegate : AdapterDelegate<ContactHeader, ContactHeaderViewHolder>() {

    companion object {
        const val CONTACT_HEADER_VIEW_TYPE = 1
    }

    override val viewType: Int
        get() = CONTACT_HEADER_VIEW_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): ContactHeaderViewHolder =
        ContactHeaderViewHolder(ItemContactHeaderBinding.inflate(inflater, parent, false))
}