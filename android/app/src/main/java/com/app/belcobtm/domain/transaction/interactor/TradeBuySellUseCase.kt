package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

data class TradeBuySellUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, TradeBuySellUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.tradeBuySell(
        params.id,
        params.price,
        params.fromUsdAmount,
        params.toCoin,
        params.toCoinAmount,
        params.detailsText
    )

    data class Params(
        val id: Int,
        val price: Int,
        val fromUsdAmount: Int,
        val toCoin: String,
        val toCoinAmount: Double,
        val detailsText: String
    )
}