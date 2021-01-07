package com.app.belcobtm.domain.contacts.item

import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.features.contacts.adapter.delegate.ContactDelegate

data class Contact(
    override val id: String,
    val displayName: String,
    val photoUri: String,
    val phoneNumber: String,
    val displayNameHighlightRange: IntRange?,
    val phoneNumberHighlightRange: IntRange?
) : ListItem {

    override val type: Int
        get() = ContactDelegate.CONTACT_DELEGATE_VIEW_TYPE
}