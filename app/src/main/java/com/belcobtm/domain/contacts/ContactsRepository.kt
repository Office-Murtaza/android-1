package com.belcobtm.domain.contacts

import com.belcobtm.domain.contacts.item.Contact

interface ContactsRepository {

    suspend fun loadContacts(searchQuery: String): List<Contact>
}