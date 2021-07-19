package com.belcobtm.presentation.features.referral.contacts

import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.diffutl.ListItemDiffUtil
import com.belcobtm.presentation.features.contacts.adapter.delegate.ContactDelegate

class InviteContactsDiffUtil : ListItemDiffUtil() {

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
            isHighlightPartChanged(
                (oldItem as SelectableContact).contact,
                (newItem as SelectableContact).contact
            )
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