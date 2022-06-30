package com.belcobtm.presentation.screens.settings.referral.contacts.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.belcobtm.databinding.ItemInviteContactBinding
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.delegate.AdapterDelegate
import com.belcobtm.presentation.screens.settings.referral.contacts.holder.InviteContactsViewHolder

class InviteContactsDelegate(
    private val contactClickListener: (SelectableContact) -> Unit
) : AdapterDelegate<SelectableContact, InviteContactsViewHolder>() {

    companion object {
        const val CONTACT_DELEGATE_VIEW_TYPE = 2
    }

    override val viewType: Int
        get() = CONTACT_DELEGATE_VIEW_TYPE

    override fun createHolder(
        parent: ViewGroup,
        inflater: LayoutInflater
    ): InviteContactsViewHolder =
        InviteContactsViewHolder(
            ItemInviteContactBinding.inflate(inflater, parent, false),
            contactClickListener
        )
}