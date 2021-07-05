package com.belcobtm.presentation.features.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.contacts.GetContactsUseCase
import com.belcobtm.domain.contacts.item.Contact
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.core.formatter.PhoneNumberFormatter
import com.belcobtm.presentation.core.validator.Validator

class ContactListViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val phoneNumberValidator: Validator<String>,
    private val phoneNumberFormatter: PhoneNumberFormatter
) : ViewModel() {
    private val _contacts = MutableLiveData<List<ListItem>>()
    val contacts: LiveData<List<ListItem>>
        get() = _contacts

    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?>
        get() = _selectedContact

    fun loadContacts(query: String = "") {
        if (query.contains("+")) {
            getContactsUseCase.invoke(getRawQuery(query), onSuccess = _contacts::setValue)
        } else {
            getContactsUseCase.invoke(query, onSuccess = _contacts::setValue)
        }
    }

    fun clearSelectedContact() {
        _selectedContact.value = null
    }

    fun selectContact(contact: Contact) {
        _selectedContact.value = contact
    }

    fun getFormattedPhoneNumber(phone: String): String =
        phoneNumberFormatter.format(phone)

    fun isValidMobileNumber(phoneNumber: String) =
        phoneNumberValidator.isValid(phoneNumber)

    private fun getRawQuery(query: String) =
        query.replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
}