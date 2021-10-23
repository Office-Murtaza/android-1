package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class SwapUseCase(
    private val repository: TransactionRepository
) : UseCase<Unit, SwapUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Unit> = repository.exchange(
        params.useMaxAmountFlag,
        params.coinFromAmount,
        params.coinToAmount,
        params.coinFrom,
        params.coinTo,
        params.fee,
        params.transactionPlanItem
    )

    data class Params(
        val useMaxAmountFlag: Boolean,
        val coinFromAmount: Double,
        val coinToAmount: Double,
        val coinFrom: String,
        val fee: Double,
        val transactionPlanItem: TransactionPlanItem,
        val coinTo: String
    )
}
