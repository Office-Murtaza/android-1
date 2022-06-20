package com.belcobtm.presentation.features.bank_accounts.ach

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.bank_account.interactor.GetLinkTokenUseCase
import com.belcobtm.domain.bank_account.interactor.LinkBankAccountUseCase
import com.belcobtm.domain.bank_account.item.BankAccountLinkDataItem
import com.belcobtm.presentation.core.SingleLiveData
import com.belcobtm.presentation.core.mvvm.LoadingData

class BankAchViewModel(
    private val getLinkTokenUseCase: GetLinkTokenUseCase,
    private val linkBankAccountUseCase: LinkBankAccountUseCase
) : ViewModel() {

    private val _linkToken = SingleLiveData<LoadingData<String>>()
    val linkToken: LiveData<LoadingData<String>> = _linkToken

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

    fun linkBankAccounts(linkBankAccountDataItem: BankAccountLinkDataItem) {
        linkBankAccountUseCase.invoke(
            LinkBankAccountUseCase.Params(linkBankAccountDataItem),
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
//                _bankAccountsLiveData.value = LoadingData.Error(it)
            }
        )
    }

}
