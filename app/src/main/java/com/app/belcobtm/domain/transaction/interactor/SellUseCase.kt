package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class SellUseCase(private val repository: TransactionRepository) : UseCase<Unit, SellUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.sell(params.coinFrom, params.coinFromAmount)

    data class Params(
        val coinFrom: String,
        val coinFromAmount: Double
    )
}