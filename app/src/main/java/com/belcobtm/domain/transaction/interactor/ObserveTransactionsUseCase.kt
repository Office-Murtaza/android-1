package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.wallet.isTrx
import com.belcobtm.presentation.screens.wallet.transactions.item.TransactionsAdapterItem
import com.belcobtm.presentation.screens.wallet.transactions.item.mapToUiItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTransactionsUseCase(private val repository: TransactionRepository) {

    fun invoke(coinCode: String): Flow<List<TransactionsAdapterItem>> =
        repository.observeTransactions()
            .map { data ->
                val transactions =
                    if (coinCode.isTrx()) data.trxTransactions
                    else data.transactionsMap.values
                        .filter { it.coinCode == coinCode }
                        .sortedByDescending { it.timestamp }

                transactions.map(TransactionDomainModel::mapToUiItem)
            }

}
