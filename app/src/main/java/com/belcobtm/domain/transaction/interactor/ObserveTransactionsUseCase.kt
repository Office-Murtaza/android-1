package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsAdapterItem
import com.belcobtm.presentation.screens.wallet.transactions.item.mapToUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {

    fun invoke(coinCode: String): Flow<List<TransactionsAdapterItem>> =
        repository.observeTransactions()
            .map { transactionsData ->
                transactionsData.transactions.values
                    .filter { it.coinCode == coinCode }
                    .sortedByDescending { it.timestamp }
                    .map(TransactionDetailsDataItem::mapToUiItem)
            }
            .flowOn(Dispatchers.Default)
}