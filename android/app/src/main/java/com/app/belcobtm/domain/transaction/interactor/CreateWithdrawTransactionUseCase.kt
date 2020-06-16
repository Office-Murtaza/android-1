package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository

class CreateWithdrawTransactionUseCase(private val repository: TransactionRepository) :
    UseCase<String, CreateWithdrawTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createWithdrawTransaction(params.fromCoin, params.fromCoinAmount, params.toAddress)

    data class Params(
        val fromCoin: String,
        val fromCoinAmount: Double,
        val toAddress: String
    )
}