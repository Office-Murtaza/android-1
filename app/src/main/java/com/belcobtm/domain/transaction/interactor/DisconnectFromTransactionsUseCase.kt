package com.belcobtm.domain.transaction.interactor

import com.belcobtm.data.websockets.transactions.TransactionsObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class DisconnectFromTransactionsUseCase(
    private val transactionsObserver: TransactionsObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(transactionsObserver.disconnect())
}