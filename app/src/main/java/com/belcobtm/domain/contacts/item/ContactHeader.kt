package com.belcobtm.domain.contacts.item

import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.features.contacts.adapter.delegate.ContactHeaderDelegate.Companion.CONTACT_HEADER_VIEW_TYPE

data class ContactHeader(val title: String) : ListItem {

    override val id: String
        get() = title

    override val type: Int
        get() = CONTACT_HEADER_VIEW_TYPE
}