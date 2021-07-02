package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class CreateTransactionUseCase(private val repository: TransactionRepository) :
    UseCase<String, CreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createTransaction(params.fromCoin, params.fromCoinAmount, params.isNeedSendSms)

    data class Params(
        val fromCoin: String,
        val fromCoinAmount: Double,
        val isNeedSendSms: Boolean = true
    )
}