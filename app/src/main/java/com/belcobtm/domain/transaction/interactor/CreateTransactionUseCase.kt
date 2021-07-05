package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository

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