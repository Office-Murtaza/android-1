package com.belcobtm.domain.referral

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.contacts.ContactsRepository
import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.domain.contacts.item.ContactHeader
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.model.ListItem

class SearchAvailableContactsUseCase(
    private val contactsRepository: ContactsRepository
) : UseCase<List<ListItem>, SearchAvailableContactsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, List<ListItem>> =
        Either.Right(
            populateHeaders(
                contactsRepository.loadContacts(params.query),
                params.existedContacts,
                params.selectedContacts
            )
        )

    private fun populateHeaders(
        contacts: List<Contact>,
        existedContacts: List<String>,
        selectedContacts: List<SelectableContact>
    ): List<ListItem> {
        val existedPhones = existedContacts.toHashSet()
        val selectedContactsMap = selectedContacts.associateByTo(HashMap()) {
            it.contact.phoneNumber
        }
        val filteredContacts = contacts.filter { contact ->
            !existedPhones.contains(contact.phoneNumber.replace("[-() ]".toRegex(), ""))
        }
        val list = ArrayList<ListItem>()
        for (i in filteredContacts.indices) {
            val contact = filteredContacts[i]
            val firstLetterCurrentContact = contact.displayName.firstOrNull()?.lowercase()
            val firstLatterNextContact =
                filteredContacts.getOrNull(i + 1)?.displayName?.firstOrNull()?.lowercase()
            if (i == 0 && firstLetterCurrentContact != null) {
                list.add(ContactHeader(firstLetterCurrentContact.uppercase()))
            }
            list.add(
                SelectableContact(
                    contact, selectedContactsMap[contact.phoneNumber]?.isSelected ?: false
                )
            )
            if (firstLetterCurrentContact != firstLatterNextContact && firstLatterNextContact != null) {
                list.add(ContactHeader(firstLatterNextContact.uppercase()))
            }
        }
        return list
    }

    data class Params(
        val query: String,
        val existedContacts: List<String>,
        val selectedContacts: List<SelectableContact>
    )
}