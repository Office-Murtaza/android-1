package com.belcobtm.domain.transaction.interactor.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class TradeReserveTransactionCreateUseCase(private val repository: TransactionRepository) :
    UseCase<String, TradeReserveTransactionCreateUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, String> =
        repository.tradeReserveTransactionCreate(
            useMaxAmountFlag = params.useMaxAmountFlag,
            coinCode = params.coinCode,
            cryptoAmount = params.cryptoAmount,
            transactionPlanItem = params.transactionPlanItem
        )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val coinCode: String,
        val cryptoAmount: Double,
        val transactionPlanItem: TransactionPlanItem
    )

}
