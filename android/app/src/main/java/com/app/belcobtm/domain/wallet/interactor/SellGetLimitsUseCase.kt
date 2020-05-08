package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.SellLimitsDataItem

class SellGetLimitsUseCase(
    private val repository: WalletRepository
) : UseCase<SellLimitsDataItem, SellGetLimitsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, SellLimitsDataItem> =
        repository.sellGetLimits(params.coinFrom)

    data class Params(val coinFrom: String)
}