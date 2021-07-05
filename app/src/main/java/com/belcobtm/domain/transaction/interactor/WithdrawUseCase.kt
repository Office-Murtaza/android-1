package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class WithdrawUseCase(
    private val repository: TransactionRepository
) :
    UseCase<Unit, WithdrawUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.withdraw(params.fromCoin, params.fromCoinAmount, params.toAddress)

    data class Params(
        val fromCoin: String,
        val fromCoinAmount: Double,
        val toAddress: String
    )
}