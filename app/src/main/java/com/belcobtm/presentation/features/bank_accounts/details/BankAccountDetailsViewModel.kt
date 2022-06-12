package com.belcobtm.presentation.features.bank_accounts.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.belcobtm.domain.Failure
import com.belcobtm.domain.bank_account.interactor.GetBankAccountPaymentsUseCase
import com.belcobtm.domain.bank_account.interactor.ObserveBankAccountDetailsUseCase
import com.belcobtm.domain.bank_account.interactor.ObservePaymentsUseCase
import com.belcobtm.domain.bank_account.item.BankAccountDataItem
import com.belcobtm.domain.bank_account.item.BankAccountLimitDataItem
import com.belcobtm.domain.bank_account.item.BankAccountPaymentListItem
import com.belcobtm.presentation.core.mvvm.LoadingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map

class BankAccountDetailsViewModel(
    val bankAccountId: String,
    val getPaymentsUseCase: GetBankAccountPaymentsUseCase,
    val bankAccountDetailsUseCase: ObserveBankAccountDetailsUseCase,
    val paymentsUseCase: ObservePaymentsUseCase,
) : ViewModel() {

    var bankAccountDataItem: BankAccountDataItem? = null
    var isExpanded = false

    private val _bankAccountPaymentsLiveData =
        MutableLiveData<LoadingData<List<BankAccountPaymentListItem>>>()
    val bankAccountPaymentsLiveData: LiveData<LoadingData<List<BankAccountPaymentListItem>>> =
        _bankAccountPaymentsLiveData

    val observeBankAccountDetailsLiveData: LiveData<LoadingData<BankAccountDataItem>>
        get() = bankAccountDetailsUseCase.invoke(
            ObserveBankAccountDetailsUseCase.Params(bankAccountId)
        )
            .map {
                if (it != null) {
                    bankAccountDataItem = it
                    LoadingData.Success(it)
                } else {
                    LoadingData.Error(Failure.ServerError())
                }
            }
            .asLiveData(Dispatchers.Default)

    val observePaymentsLiveData: LiveData<LoadingData<List<BankAccountPaymentListItem>>>
        get() = paymentsUseCase.invoke(
            ObservePaymentsUseCase.Params(bankAccountId)
        )
            .map {
                val payments = it.sortedBy { it.timestamp }
                _bankAccountPaymentsLiveData.postValue(LoadingData.Success(payments))
                LoadingData.Success(payments)

            }
            .asLiveData(Dispatchers.Default)


    var walletId: String? = null
    var feePercent: Int? = null
    var walletAddress: String? = null
    var limits: BankAccountLimitDataItem? = null
    var isInitialized = false

    fun getBankAccountPayments(accountId: String) {
        _bankAccountPaymentsLiveData.value = LoadingData.Loading()
        getPaymentsUseCase.invoke(GetBankAccountPaymentsUseCase.Params(accountId),
            onSuccess = {
                isInitialized = true
                walletAddress = it.walletAddress
                walletId = it.walletId
                feePercent = it.feePercent
                limits = it.limit
                _bankAccountPaymentsLiveData.value = LoadingData.Success(
                    it.payments
                )
            },
            onError = {
                _bankAccountPaymentsLiveData.value = LoadingData.Error(it)
            }
        )
    }
}