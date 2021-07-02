package com.app.belcobtm.domain.transaction.interactor.trade

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class TradeReserveTransactionCompleteUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, TradeReserveTransactionCompleteUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.tradeReserveTransactionComplete(params.coinCode, params.cryptoAmount, params.hash)

    data class Params(val coinCode: String, val cryptoAmount: Double, val hash: String)
}