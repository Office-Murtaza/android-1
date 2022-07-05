package com.belcobtm.domain.transaction.interactor.trade

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class TradeReserveTransactionCompleteUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, TradeReserveTransactionCompleteUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> =
        repository.tradeReserveTransactionComplete(
            coinCode = params.coinCode,
            cryptoAmount = params.cryptoAmount,
            hash = params.hash,
            fee = params.fee,
            transactionPlanItem = params.transactionPlanItem,
            price = params.price
        )

    data class Params(
        val coinCode: String,
        val cryptoAmount: Double,
        val hash: String,
        val fee: Double,
        val transactionPlanItem: TransactionPlanItem,
        val price: Double
    )

}
