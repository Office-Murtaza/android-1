package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.item.CoinDataItem
import com.app.belcobtm.domain.wallet.WalletRepository

class GetCoinListUseCase(private val repository: WalletRepository) : UseCase<List<CoinDataItem>, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, List<CoinDataItem>> = repository.getCoinList()
}