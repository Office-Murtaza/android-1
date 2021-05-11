package com.app.belcobtm.data.inmemory.transactions

import com.app.belcobtm.data.model.transactions.TransactionsData
import com.app.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.app.belcobtm.data.rest.transaction.response.mapToDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    private val cache = MutableStateFlow(TransactionsData(emptyMap()))

    val observableData: Flow<TransactionsData>
        get() = cache

    fun init(transactions: List<TransactionDetailsResponse>) {
        cache.value = TransactionsData(transactions.associateByTo(
            HashMap(), { (it.txId ?: it.txDbId).orEmpty() }) { it.mapToDataItem() }
        )
    }

    fun update(response: TransactionDetailsResponse) {
        val transactions = HashMap(cache.value.transactions)
        val id = response.txId ?: response.txDbId
        id?.let {
            transactions[id] = response.mapToDataItem()
            cache.value = cache.value.copy(transactions = transactions)
        }
    }
}