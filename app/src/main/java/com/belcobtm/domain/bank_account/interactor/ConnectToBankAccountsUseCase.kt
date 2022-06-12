package com.belcobtm.domain.bank_account.interactor

import com.belcobtm.data.websockets.bank_account.BankAccountObserver
import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase

class ConnectToBankAccountsUseCase(
    private val bankAccountsObserver: BankAccountObserver
) : UseCase<Unit, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, Unit> =
        Either.Right(bankAccountsObserver.connect())
}