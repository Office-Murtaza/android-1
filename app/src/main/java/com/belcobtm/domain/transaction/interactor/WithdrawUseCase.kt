package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class WithdrawUseCase(
    private val repository: TransactionRepository
) :
    UseCase<Unit, WithdrawUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.withdraw(
            params.useMaxAmountFlag,
            params.toAddress, params.fromCoin,
            params.fromCoinAmount, params.fee,
            params.transactionPlanItem
        )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val fromCoin: String,
        val fromCoinAmount: Double,
        val toAddress: String,
        val fee: Double,
        val transactionPlanItem: TransactionPlanItem
    )
}