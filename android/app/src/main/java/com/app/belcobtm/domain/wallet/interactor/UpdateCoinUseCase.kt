package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class UpdateCoinUseCase(private val repository: WalletRepository) : UseCase<Unit, UpdateCoinUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.updateCoin(params.dataItem)

    data class Params(val dataItem: CoinDataItem)
}