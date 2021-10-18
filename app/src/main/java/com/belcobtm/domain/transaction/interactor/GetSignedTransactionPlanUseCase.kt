package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.SignedTransactionPlanItem
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class GetSignedTransactionPlanUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<SignedTransactionPlanItem, GetSignedTransactionPlanUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, SignedTransactionPlanItem> =
        transactionRepository.getSignedPlan(
            params.coinCode,
            params.coinAmount,
            params.transactionPlanItem,
            params.toAddress,
            params.useMaxAmount
        )

    data class Params(
        val toAddress: String,
        val coinCode: String,
        val coinAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
        val useMaxAmount: Boolean,
    )
}