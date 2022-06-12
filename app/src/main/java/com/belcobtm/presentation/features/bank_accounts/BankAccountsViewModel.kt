package com.belcobtm.presentation.features.bank_accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.interactor.GetBankAccountsListUseCase
import com.belcobtm.domain.bank_account.interactor.GetLinkTokenUseCase
import com.belcobtm.domain.bank_account.interactor.LinkBankAccountUseCase
import com.belcobtm.domain.bank_account.interactor.ObserveBankAccountsListUseCase
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.domain.bank_account.item.toListItem
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

class BankAccountsViewModel(
    private val getBankAccountsListUseCase: GetBankAccountsListUseCase,
    private val getLinkTokenUseCase: GetLinkTokenUseCase,
    private val observeBankAccountsListUseCase: ObserveBankAccountsListUseCase,
    private val linkBankAccountUseCase: LinkBankAccountUseCase,
) : ViewModel() {

    private val _bankAccountsLiveData = MutableLiveData<LoadingData<List<BankAccountListItem>>>()
    val bankAccountsLiveData: LiveData<LoadingData<List<BankAccountListItem>>> =
        _bankAccountsLiveData

    private val _linkToken = SingleLiveData<LoadingData<String>>()
    val linkToken: LiveData<LoadingData<String>> = _linkToken

    val observeBankAccountsLiveData: LiveData<LoadingData<List<BankAccountListItem>>>
        get() = observeBankAccountsListUseCase.invoke(
        )
            .map {
                if (it != null) {
                    val bankAccounts =
                        it.map { bankAccount -> bankAccount.toListItem() }.sortedBy { it.createdAt }
                            .reversed()
                    _bankAccountsLiveData.postValue(LoadingData.Success(bankAccounts))
                    LoadingData.Success(bankAccounts)
                } else {
                    LoadingData.Error(Failure.ServerError())
                }
            }
            .asLiveData(Dispatchers.Default)

    init {
        getBankAccountsList()
    }

    fun getBankAccountsList() {
        _bankAccountsLiveData.value = LoadingData.Loading()
        getBankAccountsListUseCase.invoke(Unit,
            onSuccess = {
                _bankAccountsLiveData.value = LoadingData.Success(
                    it.map { bankAccountDataItem ->
                        bankAccountDataItem.toListItem()
                    }.reversed()
                )
            },
            onError = {
                _bankAccountsLiveData.value = LoadingData.Error(it)
            }
        )
    }

    fun linkBankAccounts(linkBankAccountDataItem: BankAccountLinkDataItem) {
        linkBankAccountUseCase.invoke(LinkBankAccountUseCase.Params(linkBankAccountDataItem),
            onSuccess = {
//                val currentList = _bankAccountsLiveData.value?.commonData
//                if (currentList != null) {
//                    _bankAccountsLiveData.value = LoadingData.Success(
//                        it.map { bankAccountDataItem ->
//                            bankAccountDataItem.toListItem()
//                        } + currentList
//                    )
//                } else {
//                    _bankAccountsLiveData.value = LoadingData.Success(
//                        it.map { bankAccountDataItem ->
//                            bankAccountDataItem.toListItem()
//                        }
//                    )
//                }
            },
            onError = {
                _bankAccountsLiveData.value = LoadingData.Error(it)
            }
        )
    }

    fun getLinkToken() {
        _linkToken.value = LoadingData.Loading()
        getLinkTokenUseCase.invoke(Unit,
            onSuccess = {
                _linkToken.value = LoadingData.Success(
                    it
                )
            },
            onError = {
            }
        )
    }

//    fun addBankAccountToList(bankAccountDataItem: BankAccountDataItem) {
//        val listItem = bankAccountDataItem.toListItem()
//        val currentList = _bankAccountsLiveData.value?.commonData
//        if (currentList != null) {
//            _bankAccountsLiveData.value = LoadingData.Success(
//                listOf(
//                    listItem,
//                    *currentList.toTypedArray(),
//                )
//            )
//        } else
//            _bankAccountsLiveData.value = LoadingData.Success(
//                listOf(listItem)
//            )
//    }
}



