package com.belcobtm.presentation.features.referral.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.referral.CreateRecipientsUseCase
import com.belcobtm.domain.referral.GetExistedPhoneNumbersUseCase
import com.belcobtm.domain.referral.SearchAvailableContactsUseCase
import com.belcobtm.domain.referral.item.SelectableContact
import com.belcobtm.presentation.core.adapter.model.ListItem
import com.belcobtm.presentation.tools.formatter.PhoneNumberFormatter
import com.belcobtm.presentation.core.mvvm.LoadingData

class InviteFromContactsViewModel(
    private val getAvailableContactsUseCaseUseCase: SearchAvailableContactsUseCase,
    private val getExistedPhoneNumbersUseCase: GetExistedPhoneNumbersUseCase,
    private val createRecipientsUseCase: CreateRecipientsUseCase,
    private val phoneNumberFormatter: PhoneNumberFormatter
) : ViewModel() {

    private val _contacts = MutableLiveData<List<ListItem>>()
    val contacts: LiveData<List<ListItem>>
        get() = _contacts

    private val _initialLoadingData = MutableLiveData<LoadingData<Unit>>()
    val initialLoadingData: LiveData<LoadingData<Unit>> = _initialLoadingData

    private val _coinsToReceive = MutableLiveData<Int>()
    val coinsToReceive: LiveData<Int> = _coinsToReceive

    private val _sendReferralLoadingData = MutableLiveData<LoadingData<String>>()
    val sendReferralLoadingData: LiveData<LoadingData<String>> = _sendReferralLoadingData

    private var existedContacts: List<String> = emptyList()

    fun loadInitialData(query: String = "") {
        _initialLoadingData.value = LoadingData.Loading()
        _coinsToReceive.value = 0
        getExistedPhoneNumbersUseCase.invoke(Unit, onSuccess = {
            existedContacts = it
            loadContacts(query)
            _initialLoadingData.value = LoadingData.Success(Unit)
        }, onError = {
            _initialLoadingData.value = LoadingData.Error(it)
        })
    }

    fun loadContacts(query: String = "") {
        if (query.contains("+")) {
            getAvailableContactsUseCaseUseCase.invoke(
                SearchAvailableContactsUseCase.Params(
                    getRawQuery(query), existedContacts,
                    _contacts.value?.filterIsInstance<SelectableContact>().orEmpty()
                ),
                onSuccess = _contacts::setValue
            )
        } else {
            getAvailableContactsUseCaseUseCase.invoke(
                SearchAvailableContactsUseCase.Params(
                    query, existedContacts,
                    _contacts.value?.filterIsInstance<SelectableContact>().orEmpty()
                ),
                onSuccess = _contacts::setValue
            )
        }
    }

    fun selectContact(contact: SelectableContact) {
        _contacts.value = _contacts.value?.map {
            if (it is SelectableContact && it.id == contact.id) {
                it.copy(isSelected = !contact.isSelected)
            } else {
                it
            }
        }
        val selectedItems = _contacts.value
            ?.filter { it is SelectableContact && it.isSelected }
            ?.count()
            ?: 0
        _coinsToReceive.value = selectedItems * 100
    }

    fun getFormattedPhoneNumber(phone: String): String =
        phoneNumberFormatter.format(phone)

    fun createFormattedRecipients() {
        createRecipientsUseCase.invoke(_contacts.value.orEmpty(), onSuccess = {
            _sendReferralLoadingData.value = LoadingData.Success(it)
        }, onError = {
            _sendReferralLoadingData.value = LoadingData.Error(it)
        })
    }

    private fun getRawQuery(query: String) =
        query.replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace(" ", "")
}