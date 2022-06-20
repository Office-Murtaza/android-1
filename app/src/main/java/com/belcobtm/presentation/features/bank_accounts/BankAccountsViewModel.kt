package com.belcobtm.presentation.features.bank_accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.belcobtm.domain.bank_account.interactor.GetBankAccountsListUseCase
import com.belcobtm.domain.bank_account.interactor.ObserveBankAccountsListUseCase
import com.belcobtm.domain.bank_account.item.BankAccountListItem
import com.belcobtm.domain.bank_account.item.toListItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

class BankAccountsViewModel(
    private val getBankAccountsListUseCase: GetBankAccountsListUseCase,
    private val observeBankAccountsListUseCase: ObserveBankAccountsListUseCase,
) : ViewModel() {

    private val _bankAccountsLiveData = MutableLiveData<LoadingData<List<BankAccountListItem>>>()
    val bankAccountsLiveData: LiveData<LoadingData<List<BankAccountListItem>>> =
        _bankAccountsLiveData

    val observeBankAccountsLiveData: LiveData<LoadingData<List<BankAccountListItem>>>
        get() = observeBankAccountsListUseCase.invoke()
            .map {
                val bankAccounts =
                    it.map { bankAccount -> bankAccount.toListItem() }.sortedBy { it.timestamp }
                        .reversed()
                _bankAccountsLiveData.postValue(LoadingData.Success(bankAccounts))
                LoadingData.Success(bankAccounts)
            }
            .asLiveData(Dispatchers.Default)

    init {
        getBankAccountsList()
    }

    private fun getBankAccountsList() {
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

}
