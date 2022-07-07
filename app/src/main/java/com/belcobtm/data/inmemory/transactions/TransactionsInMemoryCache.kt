package com.belcobtm.data.inmemory.transactions

import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    /**
     * We need to show transactions in the order they are given from the Node through the backend,
     * but all new transactions have to be added to the beginning.
     * Thus, I fill @see[LinkedHashMap] in reverse order of ResponseList.
     * To add then new created transactions to the top of LinkedHashMap.
     * And show them to user in again reversed order.
     *
     * I minimize Kotlin sugar here to preserve max optimization
     */

    private val cache = MutableStateFlow(emptyMap<String, TransactionDomainModel>())
    val observableData: Flow<Map<String, TransactionDomainModel>> = cache

    fun init(
        coinCode: String,
        response: List<TransactionDetailsResponse>
    ) {
        val map = LinkedHashMap<String, TransactionDomainModel>()
        for (i in response.lastIndex downTo 0) {
            val id = response[i].hash ?: response[i].gbId.orEmpty()
            map[id] = response[i].mapToDomainModel(coinCode)
        }
        cache.value = map
    }

    fun update(response: TransactionDetailsResponse) {
        val transactions = LinkedHashMap(cache.value)
        (response.hash ?: response.gbId)?.let { id ->
            transactions[id] = response.mapToDomainModel(response.coin.orEmpty())
            cache.value = transactions
        }
    }

}
