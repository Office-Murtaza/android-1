package com.app.belcobtm.domain.contacts

import com.app.belcobtm.domain.contacts.item.Contact

interface ContactsRepository {

    suspend fun loadContacts(searchQuery: String): List<Contact>
}