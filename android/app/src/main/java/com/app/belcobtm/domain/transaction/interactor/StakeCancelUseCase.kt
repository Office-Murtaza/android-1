package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class StakeCancelUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeCancelUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = transactionRepository.stakeCancel(params.coinCode)

    data class Params(val coinCode: String)
}