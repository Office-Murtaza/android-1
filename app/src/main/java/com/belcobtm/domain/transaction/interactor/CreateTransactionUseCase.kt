package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class CreateTransactionUseCase(private val repository: TransactionRepository) :
    UseCase<String, CreateTransactionUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, String> =
        repository.createTransaction(
            params.useMaxAmountFlag, params.fromCoin,
            params.fromCoinAmount, params.transactionPlanItem,
            params.isNeedSendSms
        )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val fromCoin: String,
        val fromCoinAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
        val isNeedSendSms: Boolean = true
    )
}