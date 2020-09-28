package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class StakeCancelCreateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<String, StakeCancelCreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        transactionRepository.stakeCancelCreateTransaction(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}