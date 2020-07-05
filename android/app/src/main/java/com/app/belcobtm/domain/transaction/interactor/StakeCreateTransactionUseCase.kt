package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class StakeCreateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<String, StakeCreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        transactionRepository.stakeCreateTransaction(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}