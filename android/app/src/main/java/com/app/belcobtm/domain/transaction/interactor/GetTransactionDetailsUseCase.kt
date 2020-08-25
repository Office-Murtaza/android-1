package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.TransactionDetailsDataItem

class GetTransactionDetailsUseCase(
    private val transactionRepository: TransactionRepository
) :
    UseCase<TransactionDetailsDataItem, GetTransactionDetailsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, TransactionDetailsDataItem> =
        transactionRepository.getTransactionDetails(params.txId, params.coinCode)

    data class Params(val txId: String, val coinCode: String)
}