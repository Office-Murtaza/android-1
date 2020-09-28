package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class StakeCompleteTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeCompleteTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = transactionRepository.stakeCompleteTransaction(
        params.hash,
        params.coinCode,
        params.cryptoAmount
    )

    data class Params(
        val hash: String,
        val coinCode: String,
        val cryptoAmount: Double
    )
}