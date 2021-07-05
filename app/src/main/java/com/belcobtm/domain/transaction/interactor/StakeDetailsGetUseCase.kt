package com.belcobtm.domain.transaction.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.transaction.TransactionRepository
import com.belcobtm.domain.transaction.item.StakeDetailsDataItem

data class StakeDetailsGetUseCase(
    private val transactionRepository: TransactionRepository
) : UseCase<StakeDetailsDataItem, StakeDetailsGetUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, StakeDetailsDataItem> =
        transactionRepository.stakeDetails(params.coinCode)

    data class Params(val coinCode: String)
}