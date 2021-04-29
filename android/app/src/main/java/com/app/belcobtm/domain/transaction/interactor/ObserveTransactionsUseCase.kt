package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.TransactionsAdapterItem
import com.app.belcobtm.presentation.features.wallet.transactions.item.mapToUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {

    fun invoke(): Flow<List<TransactionsAdapterItem>> =
        repository.observeTransactions()
            .map { transactionsData ->
                transactionsData.transactions.values
                    .sortedByDescending { it.timestamp }
                    .map(TransactionDetailsDataItem::mapToUiItem)
            }
            .flowOn(Dispatchers.Default)
}