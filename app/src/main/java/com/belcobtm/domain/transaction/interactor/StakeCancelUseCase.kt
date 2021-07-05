package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class StakeCancelUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeCancelUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = transactionRepository.stakeCancel(params.coinCode)

    data class Params(val coinCode: String)
}