package com.belcobtm.domain.wallet.interactor

import com.belcobtm.domain.Either
import com.belcobtm.domain.Failure
import com.belcobtm.domain.UseCase
import com.belcobtm.domain.map
import com.belcobtm.domain.wallet.WalletRepository
import com.belcobtm.domain.wallet.item.CoinDataItem

class GetCoinListUseCase(
    private val walletRepository: WalletRepository
) : UseCase<List<CoinDataItem>, Unit>() {

    override suspend fun run(params: Unit): Either<Failure, List<CoinDataItem>> =
        walletRepository.getCoinItemList().map {
            it.filter(CoinDataItem::isEnabled)
        }
}