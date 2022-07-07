package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionDomainModel
import com.belcobtm.domain.wallet.isTrx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ObserveTransactionDetailsUseCase(
    private val transactionRepository: TransactionRepository
) {

    fun invoke(params: Params): Flow<TransactionDomainModel?> =
        transactionRepository.observeTransactions()
            .map { data ->
                if (params.coinCode.isTrx())
                    data.trxTransactions.firstOrNull {
                        it.hash == params.transactionId || it.gbId == params.transactionId
                    }
                else data.transactionsMap[params.transactionId]
            }.flowOn(Dispatchers.Default)

    data class Params(val transactionId: String, val coinCode: String)

}
