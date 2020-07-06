package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem

class SellGetLimitsUseCase(
    private val repository: TransactionRepository
) : UseCase<SellLimitsDataItem, Unit>() {
    override suspend fun run(unit: Unit): Either<Failure, SellLimitsDataItem> = repository.sellGetLimits()
}