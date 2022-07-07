package com.belcobtm.data.inmemory.transactions

import com.belcobtm.data.rest.transaction.response.TransactionDetailsResponse
import com.belcobtm.domain.transaction.TransactionsCacheModel
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.wallet.LocalCoinType
import com.belcobtm.domain.wallet.isTrx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TransactionsInMemoryCache {

    /**
     * We need to pass @see [LocalCoinType.TRX] transactions in the order
     * they are given from the Node through the backend
     */
    private val cache = MutableStateFlow(TransactionsCacheModel())
    val observableData: Flow<TransactionsCacheModel> = cache

    fun init(coinCode: String, response: List<TransactionDetailsResponse>) {
        if (coinCode.isTrx()) saveTrxTransactions(response)
        else saveAllTransactions(coinCode, response)
    }

    private fun saveTrxTransactions(response: List<TransactionDetailsResponse>) {
        cache.value = TransactionsCacheModel(
            trxTransactions = response.map { it.mapToDomainModel(LocalCoinType.TRX.name) }
        )
    }

    private fun saveAllTransactions(
        coinCode: String,
        response: List<TransactionDetailsResponse>
    ) {
        val transactions = response.associateByTo(
            HashMap(), { (it.hash ?: it.gbId).orEmpty() }
        ) {
            it.mapToDomainModel(coinCode)
        }
        cache.value = TransactionsCacheModel(
            transactionsMap = transactions
        )
    }

    fun update(response: TransactionDetailsResponse) {
        if (response.coin.isTrx()) updateTrxTransaction(response)
        else updateTransaction(response)
    }

    private fun updateTrxTransaction(response: TransactionDetailsResponse) {
        val transaction = response.mapToDomainModel(LocalCoinType.TRX.name)
        val trxTransactions = ArrayList<TransactionDomainModel>().apply {
            addAll(cache.value.trxTransactions)
        }
        val existingTransactionIndex =
            trxTransactions.indexOfFirst { it.hash == response.hash || it.gbId == response.gbId }
        existingTransactionIndex.takeIf { it > -1 }?.let { index ->
            trxTransactions[index] = transaction
        } ?: trxTransactions.add(0, transaction)
        cache.value = TransactionsCacheModel(
            trxTransactions = trxTransactions
        )
    }

    private fun updateTransaction(response: TransactionDetailsResponse) {
        val transactions = HashMap(cache.value.transactionsMap)
        (response.hash ?: response.gbId)?.let { id ->
            transactions[id] = response.mapToDomainModel(response.coin.orEmpty())
            cache.value = TransactionsCacheModel(
                transactionsMap = transactions
            )
        }
    }

}
