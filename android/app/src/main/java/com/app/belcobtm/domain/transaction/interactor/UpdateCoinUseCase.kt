package com.app.belcobtm.domain.transaction.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.LocalCoinDataItem

class UpdateCoinUseCase(private val repository: WalletRepository) : UseCase<Unit, UpdateCoinUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, Unit> = repository.updateCoin(params.dataItemLocal)

    data class Params(val dataItemLocal: LocalCoinDataItem)
}