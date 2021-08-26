package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class SellUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, SellUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        with(params) {
            repository.sell(coin, coinAmount, usdAmount, fee)
        }

    data class Params(
        val coin: String,
        val price: Double,
        val coinAmount: Double,
        val usdAmount: Double,
        val fee: Double
    )
}