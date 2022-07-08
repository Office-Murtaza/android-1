package com.belcobtm.domain.contacts.item

import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.screens.contacts.adapter.delegate.ContactDelegate

data class Contact(
    override val id: String,
    val displayName: String,
    val photoUri: String,
    var phoneNumber: String,
    val displayNameHighlightRange: IntRange?,
    val phoneNumberHighlightRange: IntRange?
) : ListItem {

    override val type: Int
        get() = ContactDelegate.CONTACT_DELEGATE_VIEW_TYPE
}