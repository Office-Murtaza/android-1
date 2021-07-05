package com.belcobtm.domain.transaction.interactor.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class TradeRecallTransactionCompleteUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, TradeRecallTransactionCompleteUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.tradeRecallTransactionComplete(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}