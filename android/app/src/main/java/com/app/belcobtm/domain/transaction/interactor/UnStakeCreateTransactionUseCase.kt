package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class UnStakeCreateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<String, UnStakeCreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        transactionRepository.unStakeCreateTransaction(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}