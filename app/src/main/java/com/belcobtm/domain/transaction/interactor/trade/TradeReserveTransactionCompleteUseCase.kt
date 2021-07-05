package com.belcobtm.domain.transaction.interactor.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class TradeReserveTransactionCompleteUseCase(private val repository: TransactionRepository) :
    UseCase<Unit, TradeReserveTransactionCompleteUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.tradeReserveTransactionComplete(params.coinCode, params.cryptoAmount, params.hash)

    data class Params(val coinCode: String, val cryptoAmount: Double, val hash: String)
}