package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.data.websockets.transactions.TransactionsObserver
import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase

class ConnectToTransactionsUseCase(
    private val transactionsObserver: TransactionsObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(transactionsObserver.connect())
}