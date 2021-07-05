package com.belcobtm.domain.transaction.interactor.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

class TradeReserveTransactionCreateUseCase(private val repository: TransactionRepository) :
    UseCase<String, TradeReserveTransactionCreateUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.tradeReserveTransactionCreate(params.coinCode, params.cryptoAmount)

    data class Params(val coinCode: String, val cryptoAmount: Double)
}