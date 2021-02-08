package com.app.belcobtm.domain.wallet.interactor

import com.app.belcobtm.domain.Either
import com.app.belcobtm.domain.Failure
import com.app.belcobtm.domain.UseCase
import com.app.belcobtm.domain.wallet.WalletRepository
import com.app.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinListUseCase(private val walletRepository: WalletRepository) : UseCase<List<CoinDataItem>, Unit>() {
    override suspend fun run(params: Unit): Either<Failure, List<CoinDataItem>> =
        walletRepository.getCoinItemList()
}