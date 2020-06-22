package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.SellLimitsDataItem

class SellGetLimitsUseCase(
    private val repository: TransactionRepository
) : UseCase<SellLimitsDataItem, SellGetLimitsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, SellLimitsDataItem> =
        repository.sellGetLimits(params.coinFrom)

    data class Params(val coinFrom: String)
}