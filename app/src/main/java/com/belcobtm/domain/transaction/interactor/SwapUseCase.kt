package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

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
