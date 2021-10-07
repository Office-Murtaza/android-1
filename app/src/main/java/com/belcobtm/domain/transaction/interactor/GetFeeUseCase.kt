package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class GetFeeUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Double, GetFeeUseCase.Params>() {

    override suspend fun run(params: Params): Either<Failure, Double> =
        transactionRepository.getFee(
            params.coinCode,
            params.coinAmount,
            params.transactionPlanItem,
            params.toAddress
        )

    data class Params(
        val toAddress: String,
        val coinCode: String,
        val coinAmount: Double,
        val transactionPlanItem: TransactionPlanItem
    )
}