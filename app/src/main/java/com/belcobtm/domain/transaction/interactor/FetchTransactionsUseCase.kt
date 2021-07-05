package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class FetchTransactionsUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, FetchTransactionsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.fetchTransactionList(params.coinCode)

    data class Params(val coinCode: String)
}