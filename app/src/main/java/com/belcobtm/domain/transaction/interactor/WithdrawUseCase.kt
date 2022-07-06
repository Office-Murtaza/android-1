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
            useMaxAmountFlag = params.useMaxAmountFlag,
            toAddress = params.toAddress,
            fromCoin = params.fromCoin,
            fromCoinAmount = params.fromCoinAmount,
            fee = params.fee,
            fromTransactionPlan = params.transactionPlanItem,
            price = params.price,
            fiatAmount = params.fiatAmount
        )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val fromCoin: String,
        val fromCoinAmount: Double,
        val toAddress: String,
        val fee: Double,
        val transactionPlanItem: TransactionPlanItem,
        val price: Double,
        val fiatAmount: Double,
    )

}
