package com.belcobtm.data.inmemory.transactions

import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.wallet.LocalCoinType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    /**
     * We need to pass @see [LocalCoinType.TRX] transactions in the order
     * they are given from the Node through the backend
     */
    private val cache = MutableStateFlow(emptyList<TransactionDomainModel>())
    val observableData: Flow<List<TransactionDomainModel>> = cache

    fun init(coinCode: String, response: List<TransactionDetailsResponse>) {
        cache.value = response.map { it.mapToDomainModel(coinCode) }
    }

    fun update(response: TransactionDetailsResponse) {
        val transaction = response.mapToDomainModel(response.coin.orEmpty())
        val list = ArrayList<TransactionDomainModel>().apply {
            addAll(cache.value)
        }
        val existingTransactionIndex =
            list.indexOfFirst { it.hash == response.hash || it.gbId == response.gbId }
        existingTransactionIndex.takeIf { it > -1 }?.let { index ->
            list[index] = transaction
        } ?: list.add(0, transaction)
        cache.value = list
    }

}
