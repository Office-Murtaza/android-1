package com.belcobtm.domain.referral.item

import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.presentation.core.adapter.model.ListItem

data class SelectableContact(
    val contact: Contact,
    val isSelected: Boolean
) : ListItem by contact