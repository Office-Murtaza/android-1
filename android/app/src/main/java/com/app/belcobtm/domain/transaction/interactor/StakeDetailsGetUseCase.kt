package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.transaction.TransactionRepository
import com.app.belcobtm.domain.transaction.item.StakeDetailsDataItem

data class StakeDetailsGetUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<StakeDetailsDataItem, StakeDetailsGetUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, StakeDetailsDataItem> =
        transactionRepository.stakeDetails(params.coinCode)

    data class Params(val coinCode: String)
}