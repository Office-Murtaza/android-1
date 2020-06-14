package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class WithdrawUseCase(private val repository: TransactionRepository) : UseCase<Unit, WithdrawUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.withdraw(
        params.smsCode,
        params.hash,
        params.coinFrom,
        params.coinFromAmount
    )

    data class Params(
        val smsCode: String,
        val hash: String,
        val coinFrom: String,
        val coinFromAmount: Double
    )
}