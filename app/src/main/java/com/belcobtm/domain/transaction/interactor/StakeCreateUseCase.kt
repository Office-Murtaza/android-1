package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class StakeCreateUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<Unit, StakeCreateUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> =
        transactionRepository.stakeCreate(
            params.coinCode,
            params.cryptoAmount,
            params.feePercent,
            params.fiatAmount,
            params.transactionPlanItem
        )

    data class Params(
        val coinCode: String,
        val cryptoAmount: Double,
        val feePercent: Double,
        val fiatAmount: Double,
        val transactionPlanItem: TransactionPlanItem,
    )
}