package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class SellUseCase(private val repository: TransactionRepository) : UseCase<Unit, SellUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.sell(params.coinFrom, params.coinFromAmount)

    data class Params(
        val coinFrom: String,
        val coinFromAmount: Double
    )
}