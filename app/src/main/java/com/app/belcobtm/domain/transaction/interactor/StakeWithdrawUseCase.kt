package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class StakeWithdrawUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeWithdrawUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        transactionRepository.stakeWithdraw(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}