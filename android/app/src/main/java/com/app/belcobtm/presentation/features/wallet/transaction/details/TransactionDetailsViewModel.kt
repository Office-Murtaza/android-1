package com.app.belcobtm.presentation.features.wallet.transaction.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.belcobtm.domain.transaction.interactor.GetTransactionDetailsUseCase
import com.app.belcobtm.presentation.core.mvvm.LoadingData

class TransactionDetailsViewModel(
    private val txId: String,
    private val coinCode: String,
    private val transactionDetailsUseCase: GetTransactionDetailsUseCase
) : ViewModel() {
    val transactionDetailsLiveData: MutableLiveData<LoadingData<TransactionDetailsFragmentItem>> =
        MutableLiveData()

    init {
        getTransactionDetails()
    }

    fun getTransactionDetails() {
        transactionDetailsLiveData.value = LoadingData.Loading()
        transactionDetailsUseCase.invoke(
            params = GetTransactionDetailsUseCase.Params(txId, coinCode),
            onSuccess = {
                transactionDetailsLiveData.value = LoadingData.Success(it.mapToUiItem())
            },
            onError = { transactionDetailsLiveData.value = LoadingData.Error(it) }
        )
    }
}