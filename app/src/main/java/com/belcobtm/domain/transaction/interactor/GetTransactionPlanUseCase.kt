package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.TransactionPlanItem

class GetTransactionPlanUseCase(
    private val repository: TransactionRepository
) : UseCase<TransactionPlanItem, String>() {

    override suspend fun run(params: String): Either<Failure, TransactionPlanItem> =
        repository.getTransactionPlan(params)
}