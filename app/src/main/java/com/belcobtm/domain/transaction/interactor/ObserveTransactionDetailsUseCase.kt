package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTransactionDetailsUseCase(
    private val transactionRepository: TransactionRepository
) {

    fun invoke(params: Params): Flow<TransactionDetailsDataItem> =
        transactionRepository.observeTransactions()
            .map { data ->
                data.transactions.values.first { it.txId == params.txId }
            }.flowOn(Dispatchers.Default)

    data class Params(val txId: String, val coinCode: String)
}