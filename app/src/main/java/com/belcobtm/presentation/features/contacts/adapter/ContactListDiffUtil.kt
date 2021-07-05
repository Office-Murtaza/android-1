package com.belcobtm.presentation.features.contacts.adapter

import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.presentation.core.adapter.diffutl.ListItemDiffUtil
import com.belcobtm.presentation.features.contacts.adapter.delegate.ContactDelegate

class ContactListDiffUtil : ListItemDiffUtil() {

    companion object {
        const val CONTACT_HIGHLIGHTED_PART_CHANGED = 123
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        return if (
            oldItem.id == newItem.id &&
            oldItem.type == newItem.type &&
            oldItem.type == ContactDelegate.CONTACT_DELEGATE_VIEW_TYPE &&
            isHighlightPartChanged(oldItem as Contact, newItem as Contact)
        ) {
            CONTACT_HIGHLIGHTED_PART_CHANGED
        } else {
            null
        }
    }

    private fun isHighlightPartChanged(oldContact: Contact, newContact: Contact): Boolean =
        oldContact.displayNameHighlightRange != newContact.displayNameHighlightRange ||
                oldContact.phoneNumberHighlightRange != newContact.phoneNumberHighlightRange
}