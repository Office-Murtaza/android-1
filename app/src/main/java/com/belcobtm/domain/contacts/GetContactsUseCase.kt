package com.belcobtm.domain.contacts

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.domain.contacts.item.ContactHeader
import com.belcobtm.presentation.core.adapter.model.ListItem

class GetContactsUseCase(
    private val repository: ContactsRepository
) : UseCase<List<ListItem>, String>() {

    override suspend fun run(params: String): Either<Failure, List<ListItem>> =
        Either.Right(populateHeaders(repository.loadContacts(params)))

    private fun populateHeaders(contacts: List<Contact>): List<ListItem> {
        val list = ArrayList<ListItem>()
        for (i in contacts.indices) {
            val firstLetterCurrentContact = contacts[i].displayName.firstOrNull()?.lowercaseChar()
            val firstLatterNextContact = contacts.getOrNull(i + 1)?.displayName?.firstOrNull()?.lowercaseChar()
            if (i == 0 && firstLetterCurrentContact != null) {
                list.add(ContactHeader(firstLetterCurrentContact.uppercaseChar().toString()))
            }
            list.add(contacts[i])
            if (firstLetterCurrentContact != firstLatterNextContact && firstLatterNextContact != null) {
                list.add(ContactHeader(firstLatterNextContact.uppercaseChar().toString()))
            }
        }
        return list
    }
}