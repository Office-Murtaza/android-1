package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDetailsDataItem

class UpdateCoinDetailsUseCase(
    private val repository: WalletRepository
) :
    UseCase<CoinDetailsDataItem, UpdateCoinDetailsUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, CoinDetailsDataItem> =
        repository.updateCoinDetails(params.coinCode)

    data class Params(val coinCode: String)
}