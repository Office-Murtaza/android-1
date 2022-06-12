package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.data.websockets.payments.PaymentsObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class DisconnectFromPaymentsUseCase (
    private val paymentsObserver: PaymentsObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(paymentsObserver.disconnect())
}