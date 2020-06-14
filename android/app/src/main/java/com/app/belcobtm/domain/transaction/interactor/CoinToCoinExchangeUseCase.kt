package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class CoinToCoinExchangeUseCase(
    private val repository: TransactionRepository
) :
    UseCase<Unit, CoinToCoinExchangeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.exchangeCoinToCoin(params.smsCode, params.coinFromAmount, params.coinFrom, params.coinTo, params.hex)

    data class Params(
        val smsCode: String,
        val coinFromAmount: Double,
        val coinFrom: String,
        val coinTo: String,
        val hex: String
    )
}