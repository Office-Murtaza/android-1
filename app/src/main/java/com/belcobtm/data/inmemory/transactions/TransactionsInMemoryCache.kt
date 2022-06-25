package com.belcobtm.data.inmemory.transactions

import com.belcobtm.data.model.transactions.TransactionsData
import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.rest.transaction.response.mapToDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    private val cache = MutableStateFlow(TransactionsData(emptyMap()))

    val observableData: Flow<TransactionsData>
        get() = cache

    fun init(coinCode: String, response: List<TransactionDetailsResponse>) {
        val transactions = response.associateByTo(
            HashMap(), { (it.id ?: it.txDBId).orEmpty() }) { it.mapToDataItem(coinCode) }
        cache.value = TransactionsData(
            cache.value.transactions.toMutableMap().apply {
                putAll(transactions)
            }
        )
    }

    fun update(response: TransactionDetailsResponse) {
        val transactions = HashMap(cache.value.transactions)
        val id = response.id ?: response.txDBId
        id?.let {
            transactions[id] = response.mapToDataItem(response.coin.orEmpty())
            cache.value = cache.value.copy(transactions = transactions)
        }
    }
}