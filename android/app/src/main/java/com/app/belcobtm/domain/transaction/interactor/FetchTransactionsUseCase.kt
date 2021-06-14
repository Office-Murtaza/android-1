package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class FetchTransactionsUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, FetchTransactionsUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.fetchTransactionList(params.coinCode)

    data class Params(val coinCode: String)
}