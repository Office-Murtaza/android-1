package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class StakeWithdrawUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeWithdrawUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        transactionRepository.stakeWithdraw(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}