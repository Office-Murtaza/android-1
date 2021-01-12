package com.app.belcobtm.presentation.features.contacts.adapter.delegate

import android.view.LayoutInflater
import android.view.ViewGroup
import com.app.belcobtm.databinding.ItemContactBinding
import com.app.belcobtm.domain.contacts.item.Contact
import com.app.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.app.belcobtm.presentation.features.contacts.adapter.holder.ContactViewHolder

class ContactDelegate(
    private val contactClickListener: (Contact) -> Unit
) : AdapterDelegate<Contact, ContactViewHolder>() {

    companion object {
        const val CONTACT_DELEGATE_VIEW_TYPE = 2
    }

    override val viewType: Int
        get() = CONTACT_DELEGATE_VIEW_TYPE

    override fun createHolder(parent: ViewGroup, inflater: LayoutInflater): ContactViewHolder =
        ContactViewHolder(ItemContactBinding.inflate(inflater, parent, false), contactClickListener)
}