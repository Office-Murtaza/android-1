package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.SellLimitsDataItem

class SellGetLimitsUseCase(
    private val repository: TransactionRepository
) : UseCase<SellLimitsDataItem, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, SellLimitsDataItem> =
        repository.sellGetLimits()
}