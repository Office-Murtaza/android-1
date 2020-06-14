package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.TransactionDataItem

class GetTransactionListUseCase(private val repository: TransactionRepository) :
    UseCase<Pair<Int, List<TransactionDataItem>>, GetTransactionListUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Pair<Int, List<TransactionDataItem>>> =
        repository.getTransactionList(params.coinCode, params.currentListSize)

    data class Params(
        val coinCode: String,
        val currentListSize: Int
    )
}