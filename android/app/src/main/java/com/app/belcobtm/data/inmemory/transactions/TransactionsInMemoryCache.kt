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

    fun init(coinCode: String, response: List<TransactionDetailsResponse>) {
        val transactions = response.associateByTo(
            HashMap(), { (it.txId ?: it.txDbId).orEmpty() }) { it.mapToDataItem(coinCode) }
        cache.value = TransactionsData(
            cache.value.transactions.toMutableMap().apply {
                putAll(transactions)
            }
        )
    }

    fun update(response: TransactionDetailsResponse) {
        val transactions = HashMap(cache.value.transactions)
        val id = response.txId ?: response.txDbId
        id?.let {
            transactions[id] = response.mapToDataItem(response.coin.orEmpty())
            cache.value = cache.value.copy(transactions = transactions)
        }
    }
}