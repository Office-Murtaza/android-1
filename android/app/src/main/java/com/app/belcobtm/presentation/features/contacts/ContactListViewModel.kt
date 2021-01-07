package com.app.belcobtm.presentation.features.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.contacts.GetContactsUseCase
import com.app.belcobtm.domain.contacts.item.Contact
import com.app.belcobtm.presentation.core.adapter.model.ListItem
import com.app.belcobtm.presentation.core.formatter.Formatter
import com.app.belcobtm.presentation.core.validator.Validator

class ContactListViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val phoneNumberValidator: Validator<String>,
    private val phoneNumberFormatter: Formatter<String>
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