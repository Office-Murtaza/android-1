package com.belcobtm.presentation.features.bank_accounts.create

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.belcobtm.domain.bank_account.interactor.CreateBankAccountUseCase
import com.belcobtm.domain.bank_account.item.BankAccountCreateDataItem
import com.belcobtm.domain.bank_account.item.BankAccountCreateResponseDataItem
import com.belcobtm.domain.bank_account.type.CreateBankAccountType
import com.belcobtm.presentation.core.mvvm.LoadingData

class BankAccountCreateViewModel(
    private val createBankAccountUseCase: CreateBankAccountUseCase
) : ViewModel() {
    var selectedCreateBankAccountType: CreateBankAccountType? = null
    val createBankAccountLiveData = MutableLiveData<LoadingData<BankAccountCreateResponseDataItem>>()

    fun onCreateBankAccountSubmit(dataItem: BankAccountCreateDataItem) {
        createBankAccountLiveData.value = LoadingData.Loading()
        createBankAccountUseCase.invoke(
            CreateBankAccountUseCase.Params(dataItem),
            onSuccess = { createBankAccountLiveData.value = LoadingData.Success(it) },
            onError = { createBankAccountLiveData.value = LoadingData.Error(it) }
        )
    }


}