package com.app.belcobtm.domain.transaction.interactor.trade

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class TradeRecallTransactionCompleteUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, TradeRecallTransactionCompleteUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.tradeRecallTransactionComplete(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}