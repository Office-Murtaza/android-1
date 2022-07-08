package com.belcobtm.presentation.screens.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.contacts.GetContactsUseCase
import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.tools.formatter.PhoneNumberFormatter
import com.belcobtm.presentation.tools.validator.Validator

class ContactListViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val phoneNumberValidator: Validator<String>,
    private val phoneNumberFormatter: PhoneNumberFormatter
) : ViewModel() {

    private val _contacts = MutableLiveData<List<ListItem>>()
    val contacts: LiveData<List<ListItem>>
        get() = _contacts

    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> = _selectedContact

    fun loadContacts(query: String = "") {
        val formattedQuery = getFormattedPhoneNumber(query.trim())
        if (formattedQuery.contains("+")) {
            getContactsUseCase.invoke(getRawQuery(formattedQuery), onSuccess = _contacts::setValue)
        } else {
            getContactsUseCase.invoke(formattedQuery, onSuccess = _contacts::setValue)
        }
    }

    fun clearSelectedContact() {
        _selectedContact.value = null
    }

    fun selectContact(contact: Contact) {
        val phoneNumber = contact.phoneNumber
        _selectedContact.value = contact.copy(phoneNumber = getFormattedPhoneNumber(phoneNumber))
        loadContacts(phoneNumber)
    }

    private fun getFormattedPhoneNumber(phone: String): String =
        phoneNumberFormatter.format(phone)

    fun isValidMobileNumber(phoneNumber: String) =
        phoneNumberValidator.isValid(phoneNumber)

    private fun getRawQuery(query: String) =
        query.replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")

}
