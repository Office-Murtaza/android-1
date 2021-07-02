package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class SwapUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, SwapUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.exchange(
        params.coinFromAmount,
        params.coinToAmount,
        params.coinFrom,
        params.coinTo
    )

    data class Params(
        val coinFromAmount: Double,
        val coinToAmount: Double,
        val coinFrom: String,
        val coinTo: String
    )
}
