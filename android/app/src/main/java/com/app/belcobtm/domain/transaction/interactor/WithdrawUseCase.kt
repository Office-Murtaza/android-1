package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class WithdrawUseCase(
    private val repository: TransactionRepository
) :
    UseCase<Unit, WithdrawUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.withdraw(params.fromCoin, params.fromCoinAmount, params.toAddress)

    data class Params(
        val fromCoin: String,
        val fromCoinAmount: Double,
        val toAddress: String
    )
}