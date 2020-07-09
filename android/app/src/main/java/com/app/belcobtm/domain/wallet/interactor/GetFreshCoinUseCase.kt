package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetFreshCoinUseCase(private val repository: WalletRepository) :
    UseCase<CoinDataItem, GetFreshCoinUseCase.Params>() {
    override suspend fun run(params: Params): Either<Failure, CoinDataItem> =
        repository.getFreshCoinDataItem(params.coinCode)

    data class Params(val coinCode: String)
}