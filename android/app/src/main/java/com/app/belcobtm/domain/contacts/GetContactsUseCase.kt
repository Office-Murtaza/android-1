package com.app.belcobtm.domain.contacts

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.contacts.item.Contact
import com.app.belcobtm.domain.contacts.item.ContactHeader
import com.app.belcobtm.presentation.core.adapter.model.ListItem

class GetContactsUseCase(
    private val repository: ContactsRepository
) : UseCase<List<ListItem>, String>() {

    override suspend fun run(params: String): Either<Failure, List<ListItem>> =
        Either.Right(populateHeaders(repository.loadContacts(params)))

    private fun populateHeaders(contacts: List<Contact>): List<ListItem> {
        val list = ArrayList<ListItem>()
        for (i in contacts.indices) {
            val firstLetterCurrentContact = contacts[i].displayName.firstOrNull()?.toLowerCase()
            val firstLatterNextContact = contacts.getOrNull(i + 1)?.displayName?.firstOrNull()?.toLowerCase()
            if (i == 0 && firstLetterCurrentContact != null) {
                list.add(ContactHeader(firstLetterCurrentContact.toUpperCase().toString()))
            }
            list.add(contacts[i])
            if (firstLetterCurrentContact != firstLatterNextContact && firstLatterNextContact != null) {
                list.add(ContactHeader(firstLatterNextContact.toUpperCase().toString()))
            }
        }
        return list
    }
}