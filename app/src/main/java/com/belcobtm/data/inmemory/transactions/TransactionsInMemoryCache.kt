package com.belcobtm.data.inmemory.transactions

import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.data.rest.transaction.response.mapToDataItem
import com.belcobtm.domain.transaction.item.TransactionDetailsDataItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    private val cache = MutableStateFlow<Map<String, TransactionDetailsDataItem>>(emptyMap())
    val observableData: Flow<Map<String, TransactionDetailsDataItem>> = cache

    fun init(coinCode: String, response: List<TransactionDetailsResponse>) {
        val transactions = response.associateByTo(
            HashMap(), { (it.hash ?: it.gbId).orEmpty() }) { it.mapToDataItem(coinCode) }
        cache.value = transactions
    }

    fun update(response: TransactionDetailsResponse) {
        val transactions = HashMap(cache.value)
        val id = response.hash ?: response.gbId
        id?.let {
            transactions[id] = response.mapToDataItem(response.coin.orEmpty())
            cache.value = transactions
        }
    }

}
